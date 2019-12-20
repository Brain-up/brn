package com.epam.brn.service.parsers.csv.converter.impl

import com.epam.brn.model.Task
import com.epam.brn.service.parsers.csv.dto.TaskCsv
import org.springframework.stereotype.Component

@Component
class TaskFullCsvToTaskModelConverter : TaskCsvToTaskModelConverter() {

    override fun convert(source: TaskCsv): Task {
        val target = super.convert(source)

        convertSerialNumber(source, target)
        convertExercise(source, target)

        return target
    }

    private fun convertSerialNumber(source: TaskCsv, target: Task) {
        target.serialNumber = source.orderNumber
    }

    private fun convertExercise(source: TaskCsv, target: Task) {
        target.exercise = exerciseService.findExerciseEntityById(source.exerciseId)
    }
}