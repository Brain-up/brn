package com.epam.brn.upload.csv.processor

import com.epam.brn.constant.ExerciseType
import com.epam.brn.constant.WordType
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.repo.TaskRepository
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.SeriesService
import com.epam.brn.upload.csv.record.SeriesOneRecord
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SeriesOneRecordProcessor(
    var seriesService: SeriesService,
    var resourceService: ResourceService,
    var exerciseService: ExerciseService,
    val taskRepository: TaskRepository
) {

    @Value(value = "\${brn.audio.file.default.path}")
    private lateinit var defaultAudioFileUrl: String

    fun process(tasks: List<SeriesOneRecord>): List<Exercise> {
        val result = tasks.map { parsedValue -> convert(parsedValue) }
        taskRepository.saveAll(result)

        return result
            .map { it.exercise!! }
            .toSet().toList()
    }

    private fun convert(source: SeriesOneRecord): Task {
        val result = Task()
        result.serialNumber = source.orderNumber
        result.exercise = prepareExercise(source)
        result.correctAnswer = prepareCorrectAnswer(source)
        result.answerOptions = prepareAnswerOptions(source.words)
        return result
    }

    private fun prepareExercise(source: SeriesOneRecord): Exercise {
        return try {
            exerciseService.findExerciseByNameAndLevel(source.exerciseName, source.level)
        } catch (e: EntityNotFoundException) {
            val newExercise = Exercise(
                name = source.exerciseName,
                level = source.level,
                series = seriesService.findSeriesForId(1L),
                exerciseType = ExerciseType.of(1L).toString()
            )
            exerciseService.save(newExercise)
            newExercise
        }
    }

    private fun prepareCorrectAnswer(source: SeriesOneRecord): Resource {
        var resource = resourceService.findFirstByWordAndAudioFileUrlLike(source.word, source.audioFileName)
        if (resource != null) {
            resource.wordType = source.wordType
            resource.pictureFileUrl = source.pictureFileName
        } else {
            resource = Resource(
                audioFileUrl = source.audioFileName,
                word = source.word,
                wordType = source.wordType,
                pictureFileUrl = source.pictureFileName
            )
        }
        resourceService.save(resource)
        return resource
    }

    private fun prepareAnswerOptions(words: List<String>): MutableSet<Resource> {
        return CollectionUtils.emptyIfNull(words)
            .asSequence()
            .filter { StringUtils.isNotEmpty(it) }
            .map { word -> word.replace("[()]".toRegex(), "") }
            .map { this.toAnswerOption(it) }
            .toMutableSet()
    }

    private fun toAnswerOption(word: String): Resource {
        return resourceService.findFirstResourceByWordLike(word)
            ?: resourceService.save(
                Resource(
                    audioFileUrl = defaultAudioFileUrl.format(word),
                    word = word,
                    wordType = WordType.UNKNOWN.toString(),
                    pictureFileUrl = null
                )
            )
    }
}
