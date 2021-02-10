package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.service.AudiometryService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/audiometrics")
@Api(value = "/audiometry", description = "Contains actions for audiometry")
class AudiometryController(val audiometryService: AudiometryService) {

    @GetMapping
    @ApiOperation("Get audiometrics with tasks")
    fun getAudiometrics() = ResponseEntity.ok()
        .body(BaseResponseDto(data = audiometryService.getAudiometrics()))
}
