package com.epam.brn.controller

import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.response.BaseSingleObjectResponse
import com.epam.brn.enums.RoleConstants
import com.epam.brn.service.TaskService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
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
@Api(value = "/tasks", tags = ["Tasks"], description = "Contains actions over tasks")
@RolesAllowed(RoleConstants.USER)
class TaskController(private val taskService: TaskService) {

    private val log = logger()

    @GetMapping
    @ApiOperation("Get all tasks by exercise id")
    fun getTasksByExerciseId(
        @RequestParam(value = "exerciseId") exerciseId: Long
    ): ResponseEntity<BaseResponse> {
        log.debug("Getting tasks for exercisedId $exerciseId")
        return ResponseEntity
            .ok()
            .body(BaseResponse(data = taskService.getTasksByExerciseId(exerciseId)))
    }

    @GetMapping(value = ["/{taskId}"])
    @ApiOperation("Get task by id")
    fun getTaskById(@PathVariable("taskId") taskId: Long): ResponseEntity<BaseSingleObjectResponse> {
        return ResponseEntity.ok()
            .body(BaseSingleObjectResponse(data = taskService.getTaskById(taskId)))
    }
}
