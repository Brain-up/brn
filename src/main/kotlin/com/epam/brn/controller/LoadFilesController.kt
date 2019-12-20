package com.epam.brn.controller

import com.epam.brn.constant.BrnParams.TASK_FILE
import com.epam.brn.constant.BrnPath.FILES
import com.epam.brn.constant.BrnPath.LOAD_FULL_TASK_FILE
import com.epam.brn.constant.BrnPath.LOAD_TASK_FILE
import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.job.csv.task.UploadFromCsvJob
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(FILES)
class LoadFilesController(private val uploadTaskFromCsvJob: UploadFromCsvJob) {

    @PostMapping(LOAD_FULL_TASK_FILE)
    fun loadFullTaskFile(@RequestParam(value = TASK_FILE) taskFile: MultipartFile): ResponseEntity<BaseResponseDto> {
        val notSavingTasks = uploadTaskFromCsvJob.loadFullTaskFile(taskFile)
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseDto(listOf(notSavingTasks)))
    }

    @PostMapping(LOAD_TASK_FILE)
    fun loadTaskFile(
        @RequestParam(value = TASK_FILE) taskFile: MultipartFile,
        @RequestParam(value = "group") exerciseId: Long,
        @RequestParam(value = "series") serialNumber: Int
    ): ResponseEntity<BaseResponseDto> {
        val notSavingTasks = uploadTaskFromCsvJob.loadTaskFile(taskFile, exerciseId, serialNumber)
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseDto(listOf(notSavingTasks)))
    }
}
