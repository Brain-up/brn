package com.epam.brn.service

import com.epam.brn.dto.response.ResourceResponse
import com.epam.brn.dto.response.TaskResponse
import com.epam.brn.enums.ExerciseType
import com.epam.brn.enums.ExerciseType.FREQUENCY_WORDS
import com.epam.brn.enums.ExerciseType.PHRASES
import com.epam.brn.enums.ExerciseType.SENTENCE
import com.epam.brn.enums.ExerciseType.SINGLE_SIMPLE_WORDS
import com.epam.brn.enums.ExerciseType.SINGLE_WORDS_KOROLEVA
import com.epam.brn.enums.ExerciseType.SYLLABLES_KOROLEVA
import com.epam.brn.enums.ExerciseType.WORDS_SEQUENCES
import com.epam.brn.enums.ExerciseType.valueOf
import com.epam.brn.enums.toMechanism
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.service.cloud.CloudService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val exerciseRepository: ExerciseRepository,
    private val resourceRepository: ResourceRepository,
    private val cloudService: CloudService,
) {
    private val log = logger()

    @Cacheable("tasksByExerciseId")
    fun getTasksByExerciseId(exerciseId: Long): List<Any> {
        val exercise: Exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow { EntityNotFoundException("No exercise found for id=$exerciseId") }
        val tasks = taskRepository.findTasksByExerciseIdWithJoinedAnswers(exerciseId)
        tasks.forEach { task -> processAnswerOptions(task) }
        return when (val type = valueOf(exercise.subGroup!!.series.type)) {
            SINGLE_SIMPLE_WORDS, FREQUENCY_WORDS, SYLLABLES_KOROLEVA, PHRASES ->
                tasks.map { task -> task.toTaskResponse(type) }
            SINGLE_WORDS_KOROLEVA ->
                tasks.map { task -> task.toDetailWordsTaskDto(type) }
            WORDS_SEQUENCES, SENTENCE ->
                tasks.map { task ->
                    task.toWordsGroupSeriesTaskDto(type, task.exercise?.template)
                }
            else -> throw EntityNotFoundException("No tasks for this `$type` exercise type")
        }
    }

    @Cacheable("tasksById")
    fun getTaskById(taskId: Long): Any {
        log.debug("Searching task with id=$taskId")
        val task =
            taskRepository.findById(taskId).orElseThrow { EntityNotFoundException("No task found for id=$taskId") }
        processAnswerOptions(task)
        return when (val type = valueOf(task.exercise!!.subGroup!!.series.type)) {
            SINGLE_SIMPLE_WORDS, FREQUENCY_WORDS, SYLLABLES_KOROLEVA, PHRASES ->
                task.toTaskResponse(type)
            SINGLE_WORDS_KOROLEVA ->
                task.toDetailWordsTaskDto(type)
            WORDS_SEQUENCES, SENTENCE ->
                task.toWordsGroupSeriesTaskDto(type, task.exercise?.template)
            else -> throw EntityNotFoundException("No tasks for this `$type` exercise type")
        }
    }

    private fun processAnswerOptions(task: Task) {
        task.answerOptions
            .forEach { resource ->
                if (!resource.pictureFileUrl.isNullOrEmpty())
                    resource.pictureFileUrl = cloudService.baseFileUrl() + "/" + resource.pictureFileUrl
            }
    }

    @CacheEvict("tasksByExerciseId", "tasksById")
    @Transactional
    fun save(task: Task): Task {
        val resources = mutableSetOf<Resource>()
        resources.addAll(task.answerOptions)
        task.correctAnswer?.let { resources.add(it) }
        resourceRepository.saveAll(resources)
        return taskRepository.save(task)
    }
}

private val vowelSet = setOf('а', 'е', 'ё', 'и', 'о', 'у', 'э', 'ы', 'ю', 'я')

fun String.findSyllableCount(): Int = count { it in vowelSet }

fun Task.toDetailWordsTaskDto(exerciseType: ExerciseType) = TaskResponse(
    id = id!!,
    exerciseType = exerciseType,
    exerciseMechanism = exerciseType.toMechanism(),
    name = name,
    serialNumber = serialNumber,
    answerOptions = answerOptions.toResourceDtoSet()
)

fun MutableSet<Resource>.toResourceDtoSet(): HashSet<ResourceResponse> {
    val mapVowelCountToWord: Map<Int, List<Resource>> =
        this.groupBy { resource -> resource.word.findSyllableCount() }
    val resultDtoSet = mutableSetOf<ResourceResponse>()
    mapVowelCountToWord.keys
        .sorted()
        .forEachIndexed { index, vowelCount ->
            val resources = mapVowelCountToWord[vowelCount]?.map { it.toResponse() }
            resources?.forEach {
                it.columnNumber = index
                it.soundsCount = vowelCount
            }
            resultDtoSet.addAll(resources ?: emptySet())
        }
    return resultDtoSet.toHashSet()
}
