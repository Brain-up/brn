package com.epam.brn.service

import com.epam.brn.dto.response.TaskResponse
import com.epam.brn.dto.response.ResourceResponse
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
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
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.TaskRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val exerciseRepository: ExerciseRepository,
    private val resourceRepository: ResourceRepository,
    private val urlConversionService: UrlConversionService,
) {
    private val log = logger()

    fun getTasksByExerciseId(exerciseId: Long): List<Any> {
        val exercise: Exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow { EntityNotFoundException("No exercise found for id=$exerciseId") }
        val tasks = taskRepository.findTasksByExerciseIdWithJoinedAnswers(exerciseId)
        tasks.forEach { task -> processAnswerOptions(task) }
        return when (val type = valueOf(exercise.subGroup!!.series.type)) {
            SINGLE_SIMPLE_WORDS, FREQUENCY_WORDS, SYLLABLES_KOROLEVA -> tasks.map { task -> task.toTaskResponse(type) }
            SINGLE_WORDS_KOROLEVA -> tasks.map { task -> task.toDetailWordsTaskDto(type) }
            WORDS_SEQUENCES -> tasks.map { task -> task.toWordsGroupSeriesTaskDto(task.exercise?.template) }
            SENTENCE -> tasks.map { task -> task.toSentenceSeriesTaskDto(task.exercise?.template) }
            PHRASES -> tasks.map { task -> task.toTaskResponse(PHRASES) }
            else -> throw EntityNotFoundException("No tasks for this `$type` exercise type")
        }
    }

    fun getTaskById(taskId: Long): Any {
        log.debug("Searching task with id=$taskId")
        val task =
            taskRepository.findById(taskId).orElseThrow { EntityNotFoundException("No task found for id=$taskId") }
        processAnswerOptions(task)
        return when (val type = valueOf(task.exercise!!.subGroup!!.series.type)) {
            SINGLE_SIMPLE_WORDS, FREQUENCY_WORDS, SYLLABLES_KOROLEVA -> task.toTaskResponse(type)
            SINGLE_WORDS_KOROLEVA -> task.toDetailWordsTaskDto(type)
            WORDS_SEQUENCES -> task.toWordsGroupSeriesTaskDto(task.exercise?.template)
            SENTENCE -> task.toSentenceSeriesTaskDto(task.exercise?.template)
            PHRASES -> task.toTaskResponse(PHRASES)
            else -> throw EntityNotFoundException("No tasks for this `$type` exercise type")
        }
    }

    private fun processAnswerOptions(task: Task) {
        task.answerOptions
            .parallelStream()
            .forEach { resource ->
                resource.pictureFileUrl = urlConversionService.makeUrlForTaskPicture(resource.word)
            }
    }

    @Transactional
    fun save(task: Task): Task {
        val resources = mutableSetOf<Resource>()
        resources.addAll(task.answerOptions)
        task.correctAnswer?.let { resources.add(it) }
        resourceRepository.saveAll(resources)
        return taskRepository.save(task)
    }
}

val vowels = "а,е,ё,и,о,у,э,ы,ю,я".toCharArray()

fun String.findSyllableCount(): Int {
    var syllableCount = 0
    this.toCharArray().forEach { if (vowels.contains(it)) syllableCount++ }
    return syllableCount
}

fun Task.toDetailWordsTaskDto(exerciseType: ExerciseType) = TaskResponse(
    id = id!!,
    exerciseType = exerciseType,
    exerciseMechanism = exerciseType.toMechanism(),
    name = name,
    serialNumber = serialNumber,
    answerOptions = answerOptions.toResourceDtos()
)

fun Iterable<Resource>.toResourceDtos(): MutableList<ResourceResponse> {
    val mapVowelCountToWord: Map<Int, List<Resource>> =
        this.groupBy { resource -> resource.word.findSyllableCount() }
    val resultDtoSet = mutableListOf<ResourceResponse>()
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
    return resultDtoSet
}
