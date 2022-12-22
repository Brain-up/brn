package com.epam.brn.controller

import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.logging.log4j.kotlin.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "Contains actions over tasks")
@RolesAllowed(BrnRole.USER)
class TaskController(private val taskService: TaskService) {

    private val log = logger()

    @GetMapping
    @Operation(summary = "Get all tasks by exercise id")
    fun getTasksByExerciseId(
        @RequestParam(value = "exerciseId") exerciseId: Long
    ): ResponseEntity<BrnResponse<List<Any>>> {
        log.debug("Getting tasks for exercisedId $exerciseId")
        return ResponseEntity
            .ok()
            .body(BrnResponse(data = taskService.getTasksByExerciseId(exerciseId)))
    }

    @GetMapping(value = ["/{taskId}"])
    @Operation(summary = "Get task by id")
    fun getTaskById(@PathVariable("taskId") taskId: Long): ResponseEntity<BrnResponse<Any>> {
        return ResponseEntity.ok()
            .body(BrnResponse(data = taskService.getTaskById(taskId)))
    }
}
