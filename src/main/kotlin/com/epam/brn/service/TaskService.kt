package com.epam.brn.service

import com.epam.brn.dto.response.ResourceResponse
import com.epam.brn.dto.response.WordsTaskResponse
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.ExerciseType.FREQUENCY_WORDS
import com.epam.brn.model.ExerciseType.PHRASES
import com.epam.brn.model.ExerciseType.SENTENCE
import com.epam.brn.model.ExerciseType.SINGLE_SIMPLE_WORDS
import com.epam.brn.model.ExerciseType.SINGLE_WORDS_KOROLEVA
import com.epam.brn.model.ExerciseType.WORDS_SEQUENCES
import com.epam.brn.model.ExerciseType.valueOf
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.TaskRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val exerciseRepository: ExerciseRepository,
    private val resourceRepository: ResourceRepository,
    private val wordsService: WordsService,
    private val urlConversionService: UrlConversionService,
) {
    private val log = logger()

    @Value(value = "\${brn.audio.file.getFromStorage}")
    private var getAudioFileFromStorage: Boolean = false

    fun getTasksByExerciseId(exerciseId: Long): List<Any> {
        val exercise: Exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow { EntityNotFoundException("No exercise found for id=$exerciseId") }
        val tasks = taskRepository.findTasksByExerciseIdWithJoinedAnswers(exerciseId)
        tasks.forEach { task -> processAnswerOptions(task) }
        return when (val type = valueOf(exercise.subGroup!!.series.type)) {
            SINGLE_SIMPLE_WORDS, FREQUENCY_WORDS -> tasks.map { task -> task.toWordsTaskDto(type) }
            SINGLE_WORDS_KOROLEVA -> tasks.map { task -> task.toDetailWordsTaskDto(type) }
            WORDS_SEQUENCES -> tasks.map { task -> task.toWordsGroupSeriesTaskDto(task.exercise?.template) }
            SENTENCE -> tasks.map { task -> task.toSentenceSeriesTaskDto(task.exercise?.template) }
            PHRASES -> tasks.map { task -> task.toPhraseSeriesTaskDto() }
            else -> throw EntityNotFoundException("No tasks for this `$type` exercise type")
        }
    }

    fun getTaskById(taskId: Long): Any {
        log.debug("Searching task with id=$taskId")
        val task =
            taskRepository.findById(taskId).orElseThrow { EntityNotFoundException("No task found for id=$taskId") }
        processAnswerOptions(task)
        return when (val type = valueOf(task.exercise!!.subGroup!!.series.type)) {
            SINGLE_SIMPLE_WORDS, FREQUENCY_WORDS -> task.toWordsTaskDto(type)
            SINGLE_WORDS_KOROLEVA -> task.toDetailWordsTaskDto(type)
            WORDS_SEQUENCES -> task.toWordsGroupSeriesTaskDto(task.exercise?.template)
            SENTENCE -> task.toSentenceSeriesTaskDto(task.exercise?.template)
            PHRASES -> task.toPhraseSeriesTaskDto()
            else -> throw EntityNotFoundException("No tasks for this `$type` exercise type")
        }
    }

    private fun processAnswerOptions(task: Task) {
        task.answerOptions.forEach { resource ->
            if (getAudioFileFromStorage)
                resource.audioFileUrl = wordsService.getFullS3UrlForWord(resource.word, resource.locale)
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

fun Task.toDetailWordsTaskDto(exerciseType: ExerciseType) = WordsTaskResponse(
    id = id!!,
    exerciseType = exerciseType,
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
