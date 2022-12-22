package com.epam.brn.controller

import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.AudiometryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/audiometrics")
@Tag(name = "Audio Metrics", description = "Contains actions for audiometry")
@RolesAllowed(BrnRole.USER)
class AudiometryController(private val audiometryService: AudiometryService) {

    @GetMapping
    @Operation(summary = "Get audiometrics without tasks")
    fun getAudiometrics(@RequestParam(value = "locale", required = false, defaultValue = "ru-ru") locale: String) =
        ResponseEntity
            .ok()
            .body(BrnResponse(data = audiometryService.getAudiometrics(locale)))

    @GetMapping(value = ["/{audiometryId}"])
    @Operation(summary = "Get audiometry for id with tasks")
    fun getAudiometry(@PathVariable("audiometryId") audiometryId: Long) =
        ResponseEntity
            .ok()
            .body(BrnResponse(data = audiometryService.getAudiometry(audiometryId)))
}
