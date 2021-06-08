package com.epam.brn.controller

import com.epam.brn.service.YandexSpeechKitService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/audio")
@Api(value = "/audio", description = "Contains actions for getting audio file for words")
class AudioController(@Autowired private val yandexSpeechKitService: YandexSpeechKitService) {

    @GetMapping(produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @ApiOperation("Get audio resource for string")
    fun getAudioByteArray(
        @RequestParam("text", required = true) text: String,
        @RequestParam("locale", required = false, defaultValue = "ru-ru") locale: String
    ): ResponseEntity<ByteArray> {
        val inputStream = yandexSpeechKitService.generateAudioOggFileWithValidation(text, locale)
        val out = IOUtils.toByteArray(inputStream)
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(out)
    }
}
