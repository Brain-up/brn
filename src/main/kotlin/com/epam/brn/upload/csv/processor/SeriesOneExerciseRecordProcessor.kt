package com.epam.brn.upload.csv.processor

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.SeriesService
import com.epam.brn.upload.csv.record.SeriesOneTaskRecord
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SeriesOneExerciseRecordProcessor(
    var exerciseService: ExerciseService,
    var seriesService: SeriesService,
    var resourceService: ResourceService
) {

    @Value(value = "\${brn.audio.file.default.path}")
    private lateinit var defaultAudioFileUrl: String

    fun convert(source: SeriesOneTaskRecord): Task {
        val result = Task()
        result.serialNumber = source.orderNumber
        result.exercise = prepareExercise(source)
        result.correctAnswer = prepareCorrectAnswer(source)
        result.answerOptions = prepareAnswerOptions(source.words)
        return result
    }

    private fun prepareExercise(source: SeriesOneTaskRecord): Exercise {
        return try {
            exerciseService.findExerciseByNameAndLevel(source.exerciseName, source.level)
        } catch (e: EntityNotFoundException) {
            val newExercise = Exercise(
                name = source.exerciseName,
                level = source.level,
                series = seriesService.findSeriesForId(1L),
                exerciseType = ExerciseTypeEnum.of(1L).toString()
            )
            exerciseService.save(newExercise)
        }
    }

    private fun prepareCorrectAnswer(source: SeriesOneTaskRecord): Resource {
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
        return resourceService.save(resource)
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
                    wordType = WordTypeEnum.UNKNOWN.toString(),
                    pictureFileUrl = null
                )
            )
    }
}
