package com.epam.brn.service.parsers.csv.converter.impl

import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.parsers.csv.converter.Converter
import com.epam.brn.service.parsers.csv.dto.TaskCsv
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TaskCsvToTaskModelConverter : Converter<TaskCsv, Task> {

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
        target.exercise = exerciseService.findExerciseEntityById(source.exerciseId)
    }

    private fun convertCorrectAnswer(source: TaskCsv, target: Task) {
        val word = source.word
        val fileName = source.fileName
        val resources = resourceService.findByWordAndAudioFileUrlLike(word, fileName)

        if (CollectionUtils.isEmpty(resources)) {
            target.correctAnswer = createAndGetResource(word, fileName)
        } else {
            target.correctAnswer = resources[0]
        }
    }

    private fun convertAnswers(source: TaskCsv, target: Task) {
        target.answerOptions = CollectionUtils.emptyIfNull(source.words)
            .filter { StringUtils.isNotEmpty(it) }
            .map {word -> word.replace("[()]".toRegex(), "")}
            .map(this::getResourceByWord)
            .toMutableSet()
    }

    private fun getResourceByWord(word: String): Resource {
        val resources = resourceService.findByWordLike(word)

        if (CollectionUtils.isEmpty(resources)) {
            return createAndGetResource(word, StringUtils.EMPTY)
        }

        return resources[0]
    }

    private fun createAndGetResource(word: String, fileName: String): Resource {
        val resource = Resource()
        resource.word = word

        if (StringUtils.isNotEmpty(fileName)) {
            resource.audioFileUrl = fileName
        } else {
            resource.audioFileUrl = defaultAudioFileUrl.format(word)
        }

        resourceService.save(resource)

        return resource
    }
}