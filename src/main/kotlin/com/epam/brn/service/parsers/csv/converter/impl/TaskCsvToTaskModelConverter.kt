package com.epam.brn.service.parsers.csv.converter.impl

import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.parsers.csv.converter.Converter
import com.epam.brn.service.parsers.csv.dto.TaskCsv
import org.apache.commons.collections4.CollectionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TaskCsvToTaskModelConverter : Converter<TaskCsv, Task> {

    @Autowired
    private lateinit var exerciseService: ExerciseService

    @Autowired
    private lateinit var resourceService: ResourceService

    override fun convert(source: TaskCsv): Task {
        val target = Task()

        convertSerialNumber(source, target)
        convertExercise(source, target)
        convertCorrectAnswer(source, target)
        convertAnswers(source, target)

        return target
    }

    private fun convertSerialNumber(source: TaskCsv, target: Task) {
        target.serialNumber = source.orderNumber.toInt()
    }

    private fun convertExercise(source: TaskCsv, target: Task) {
        val exerciseId = source.exerciseId.toLong()
        val exercise = exerciseService.findExercisesById(exerciseId)

        if (!exercise.isPresent) throw EntityNotFoundException("Exercise with id $exerciseId was not found")

        target.exercise = exercise.get()
    }

    private fun convertCorrectAnswer(source: TaskCsv, target: Task) {
        val word = source.word
        val resources = resourceService.findByWordLike(word)

        if (CollectionUtils.isEmpty(resources)) {
            val resource = Resource()
            resource.word = word
            resource.audioFileUrl = source.fileName

            resourceService.save(resource)
            target.correctAnswer = resource
        } else {
            target.correctAnswer = resources[0]
        }
    }

    private fun convertAnswers(source: TaskCsv, target: Task) {
        target.answerOptions = CollectionUtils.emptyIfNull(source.words)
            .map(this::getResourceByWord)
            .toMutableSet()
    }

    private fun getResourceByWord(word: String): Resource {
        val resources = resourceService.findByWordLike(word)

        if (CollectionUtils.isEmpty(resources)) {
            throw EntityNotFoundException("Resource with word $word was not found")
        }

        return resources[0]
    }
}