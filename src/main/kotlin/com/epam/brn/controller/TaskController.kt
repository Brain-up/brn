package com.epam.brn.controller

import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.TaskDto
import com.epam.brn.job.csv.task.UploadFromCsvJob
import com.epam.brn.service.TaskService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(BrnPath.TASKS)
class TaskController(private val taskService: TaskService, private val uploadTaskFromCsvJob: UploadFromCsvJob) {

    private val log = logger()

    @GetMapping
    fun getTasksForExerciseId(@RequestParam(value = "exerciseId", required = false) exerciseId: Long?): List<TaskDto> {
        // TODO will be improve, search will be more flexible

        exerciseId?.let {
            return taskService.findAllTasksByExerciseIdIncludeAnswers(it)
        }
        return taskService.findAllTasksIncludeAnswers()
    }

    @PostMapping(BrnPath.TASK)
    fun addTask(@RequestParam(value = "taskFile") taskFile: MultipartFile): ResponseEntity<String> {
        uploadTaskFromCsvJob.uploadTask(taskFile)

        return ResponseEntity.ok().build()
    }
}