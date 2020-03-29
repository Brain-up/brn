package com.epam.brn.controller

import com.epam.brn.constant.BrnPath.LOAD_TASKS_FILE
import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.upload.CsvUploadService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class LoadFilesController(private val csvUploadService: CsvUploadService) {

    @PostMapping(LOAD_TASKS_FILE)
    fun loadExercises(
        @RequestParam(value = "seriesId") seriesId: Long,
        @RequestParam(value = "taskFile") file: MultipartFile
    ): ResponseEntity<BaseResponseDto> {
        csvUploadService.loadExercises(seriesId, file)
        return ResponseEntity(HttpStatus.CREATED)
    }
}
