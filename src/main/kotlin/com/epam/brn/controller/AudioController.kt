package com.epam.brn.controller

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.UserAnalyticsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.commons.io.IOUtils.toByteArray
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/audio")
@Tag(name = "Audio", description = "Contains actions for getting audio file for words")
@ConditionalOnProperty(name = ["default.tts.provider"])
@RolesAllowed(BrnRole.USER)
class AudioController(private val userAnalyticsService: UserAnalyticsService) {

    @GetMapping(produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @Operation(summary = "Get audio resource for text and exerciseId")
    fun getAudioByteArray(
        @RequestParam text: String,
        @RequestParam(required = false, defaultValue = "0") exerciseId: Long,
        @RequestParam(required = false, defaultValue = "ru-ru") locale: String,
        @RequestParam(required = false, defaultValue = "") voice: String,
        @RequestParam(required = false, defaultValue = "1.0") speed: String,
        @RequestParam(required = false) gender: String? = null,
        @RequestParam(required = false) pitch: String? = null,
        @RequestParam(required = false) style: String? = null,
    ): ResponseEntity<ByteArray> {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(
                toByteArray(
                    userAnalyticsService.prepareAudioFileForUser(
                        exerciseId,
                        AudioFileMetaData(
                            text = text,
                            locale = locale,
                            voice = voice,
                            gender = gender,
                            speedFloat = speed,
                            pitch = pitch,
                            style = style
                        )
                    )
                )
            )
    }
}
