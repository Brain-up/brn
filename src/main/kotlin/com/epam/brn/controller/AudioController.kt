package com.epam.brn.controller

import com.epam.brn.service.TextToSpeechService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.apache.commons.io.IOUtils.toByteArray
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/audio")
@Api(value = "/audio", description = "Contains actions for getting audio file for words")
@ConditionalOnProperty(name = ["default.tts.provider"])
class AudioController(private val textToSpeechService: TextToSpeechService) {

    @GetMapping(produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @ApiOperation("Get audio resource for string")
    fun getAudioByteArray(
        @RequestParam text: String,
        @RequestParam(required = false, defaultValue = "ru-ru") locale: String,
        @RequestParam(required = false, defaultValue = "") voice: String,
        @RequestParam(required = false, defaultValue = "") speed: String,
        @RequestParam(required = false) gender: String? = null,
        @RequestParam(required = false) pitch: String? = null,
        @RequestParam(required = false) style: String? = null
    ): ResponseEntity<ByteArray> {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(
                toByteArray(
                    textToSpeechService.generateAudioOggFileWithValidation(
                        text = text,
                        locale = locale,
                        voice = voice,
                        gender = gender,
                        speed = speed,
                        pitch = pitch,
                        style = style
                    )
                )
            )
    }
}
