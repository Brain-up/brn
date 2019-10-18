package com.epam.brn.controller

import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.TaskDto
import com.epam.brn.service.TaskService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.TASKS)
class TaskController(private val taskService: TaskService) {

    @GetMapping
    fun getTasksForExerciseId(@RequestParam(value = "exerciseId", required = false) exerciseId: String?): List<TaskDto> {
        // TODO will be improve, search will be more flexible

        exerciseId?.let {
            return taskService.findAllTasksWithExerciseIdWithAnswers(it)
        }
        return taskService.findAllTasksWithAnswers()
    }
}