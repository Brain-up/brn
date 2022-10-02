package com.epam.brn.controller

import com.epam.brn.dto.request.AudiometryHistoryRequest
import com.epam.brn.dto.response.Response
import com.epam.brn.service.AudiometryHistoryService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/audiometry-history")
@Api(value = "/audiometryHistory", description = "Contains actions for audiometry history")
class AudiometryHistoryController(private val audiometryHistoryService: AudiometryHistoryService) {

    @PostMapping
    @ApiOperation("Save speech audiometry history")
    fun save(@Validated @RequestBody audiometryHistory: AudiometryHistoryRequest): ResponseEntity<Response<Long>> =
        ResponseEntity.ok().body(Response(data = audiometryHistoryService.save(audiometryHistory)))
}
