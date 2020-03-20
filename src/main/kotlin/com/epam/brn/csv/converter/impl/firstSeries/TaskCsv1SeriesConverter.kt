package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.csv.converter.Converter
import com.epam.brn.csv.dto.TaskCsv
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TaskCsv1SeriesConverter : Converter<TaskCsv, Task> {

    @Value(value = "\${brn.audio.file.default.path}")
    private lateinit var defaultAudioFileUrl: String

    @Autowired
    lateinit var exerciseService: ExerciseService

    @Autowired
    lateinit var resourceService: ResourceService

    override fun convert(source: TaskCsv): Task {
        val result = Task()
        result.serialNumber = source.orderNumber
        result.exercise = prepareExercise(source)
        result.correctAnswer = prepareCorrectAnswer(source)
        result.answerOptions = prepareAnswerOptions(source.words)
        return result
    }

    private fun prepareExercise(source: TaskCsv): Exercise {
        return try {
            exerciseService.findExerciseByNameAndLevel(source.exerciseName, source.level)
        } catch (e: EntityNotFoundException) {
            exerciseService.createExercise(source.exerciseName)
        }
    }

    private fun prepareCorrectAnswer(source: TaskCsv): Resource {
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
