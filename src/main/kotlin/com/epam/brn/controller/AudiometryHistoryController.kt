package com.epam.brn.controller

import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.request.AudiometryHistoryRequest
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
@RequestMapping("/audiometryHistory")
@Api(value = "/audiometryHistory", description = "Contains actions for audiometry history")
class AudiometryHistoryController(val audiometryHistoryService: AudiometryHistoryService) {

    @PostMapping
    @ApiOperation("Save audiometry history")
    fun save(@Validated @RequestBody audiometryHistory: AudiometryHistoryRequest): ResponseEntity<BaseSingleObjectResponseDto> =
        ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = audiometryHistoryService.save(audiometryHistory)))
}
