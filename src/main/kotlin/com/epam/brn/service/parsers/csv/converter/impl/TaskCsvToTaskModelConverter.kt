package com.epam.brn.service.parsers.csv.converter.impl

import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.parsers.csv.converter.Converter
import com.epam.brn.service.parsers.csv.dto.TaskCsv
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TaskCsvToTaskModelConverter : Converter<TaskCsv, Task> {

    private val log = logger()

    @Value(value = "\${brn.audio.file.default.path}")
    private lateinit var defaultAudioFileUrl: String

    @Autowired
    lateinit var exerciseService: ExerciseService

    @Autowired
    lateinit var resourceService: ResourceService

    override fun convert(source: TaskCsv): Task {
        val target = Task()
        convertSerialNumber(source, target)
        convertExercise(source, target)
        convertCorrectAnswer(source, target)
        convertAnswers(source, target)
        return target
    }

    private fun convertSerialNumber(source: TaskCsv, target: Task) {
        target.serialNumber = source.orderNumber
    }

    private fun convertExercise(source: TaskCsv, target: Task) {
        try {
            target.exercise = exerciseService.findExerciseEntityByName(source.exerciseName)
        } catch (e: EntityNotFoundException) {
            log.debug("Entity was not found by name $source.exerciseName")
            target.exercise = exerciseService.createExercise(source.exerciseName)
        }
    }

    private fun convertCorrectAnswer(source: TaskCsv, target: Task) {
        val word = source.word
        val audioFileName = source.audioFileName
        val resources = resourceService.findByWordAndAudioFileUrlLike(word, audioFileName)
        val correctAnswer: Resource
        if (CollectionUtils.isEmpty(resources)) {
            correctAnswer = createAndGetResource(word, audioFileName, source.pictureFileName)
        } else {
            correctAnswer = resources[0]
            correctAnswer.pictureFileUrl = source.pictureFileName
            resourceService.save(correctAnswer)
        }
        target.correctAnswer = correctAnswer
    }

    private fun convertAnswers(source: TaskCsv, target: Task) {
        target.answerOptions = CollectionUtils.emptyIfNull(source.words)
            .filter { StringUtils.isNotEmpty(it) }
            .map { word -> word.replace("[()]".toRegex(), "") }
            .map(this::getResourceByWord)
            .toMutableSet()
    }

    private fun getResourceByWord(word: String): Resource {
        val resources = resourceService.findByWordLike(word)
        return if (CollectionUtils.isEmpty(resources))
            createAndGetResource(word, StringUtils.EMPTY, StringUtils.EMPTY)
        else
            resources[0]
    }

    private fun createAndGetResource(word: String, audioFileName: String, pictureFileName: String): Resource {
        val resource = Resource()
        resource.word = word
        resource.audioFileUrl =
            if (StringUtils.isNotEmpty(audioFileName)) audioFileName else defaultAudioFileUrl.format(word)
        resource.pictureFileUrl =
            if (StringUtils.isNotEmpty(pictureFileName)) pictureFileName else null
        resourceService.save(resource)
        return resource
    }
}
