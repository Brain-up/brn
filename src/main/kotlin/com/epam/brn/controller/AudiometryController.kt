package com.epam.brn.controller

import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.response.BaseSingleObjectResponse
import com.epam.brn.service.AudiometryService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/audiometrics")
@Api(value = "/audiometrics", description = "Contains actions for audiometry")
class AudiometryController(private val audiometryService: AudiometryService) {

    @GetMapping
    @ApiOperation("Get audiometrics without tasks")
    fun getAudiometrics(@RequestParam(value = "locale", required = false, defaultValue = "ru-ru") locale: String) =
        ResponseEntity
            .ok()
            .body(BaseResponse(data = audiometryService.getAudiometrics(locale)))

    @GetMapping(value = ["/{audiometryId}"])
    @ApiOperation("Get audiometry for id with tasks")
    fun getAudiometry(@PathVariable("audiometryId") audiometryId: Long) =
        ResponseEntity
            .ok()
            .body(BaseSingleObjectResponse(data = audiometryService.getAudiometry(audiometryId)))
}
