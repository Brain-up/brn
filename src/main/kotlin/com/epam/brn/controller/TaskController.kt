package com.epam.brn.controller

import com.epam.brn.dto.TaskDto
import com.epam.brn.service.TaskService
import com.lifescience.brn.constant.BrnPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.TASKS)
class TaskController(@Autowired val taskService: TaskService) {

    @GetMapping
    fun getTasks(@RequestParam(value = "exerciseId", defaultValue = "0") exerciseId: String): List<TaskDto> {
        return taskService.findTasks(exerciseId)
    }
}