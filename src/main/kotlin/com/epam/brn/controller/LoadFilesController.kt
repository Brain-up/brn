package com.epam.brn.controller

import com.epam.brn.constant.BrnPath.FILES
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

    @PostMapping
    fun addTasks(@RequestParam(value = "taskFile") taskFile: MultipartFile): ResponseEntity<String> {
        uploadTaskFromCsvJob.uploadTasks(taskFile)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}