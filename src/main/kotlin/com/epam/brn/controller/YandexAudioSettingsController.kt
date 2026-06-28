package com.epam.brn.controller

import com.epam.brn.dto.request.audio.AudioVoiceOverrideRequest
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.response.audio.AudioVoiceOptionResponse
import com.epam.brn.dto.response.audio.AudioVoiceSettingsResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.WordsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/audio")
@Tag(name = "Audio", description = "Contains actions for getting audio file for words")
@ConditionalOnProperty(name = ["default.tts.provider"], havingValue = "yandex")
@RolesAllowed(BrnRole.USER)
class YandexAudioSettingsController(
    private val wordsService: WordsService,
) {
    @GetMapping("/voices")
    @Operation(summary = "Get available Yandex voices and the current runtime default for a locale")
    fun getVoices(
        @RequestParam(required = false, defaultValue = "ru-ru") locale: String,
    ): ResponseEntity<BrnResponse<AudioVoiceSettingsResponse>> = ResponseEntity
        .ok()
        .body(BrnResponse(data = buildVoiceSettingsResponse(locale)))

    @PostMapping("/default-voice")
    @Operation(summary = "Set the runtime default Yandex voice for a locale until the server restarts")
    @RolesAllowed(BrnRole.ADMIN)
    fun setDefaultVoice(
        @RequestBody request: AudioVoiceOverrideRequest,
    ): ResponseEntity<BrnResponse<AudioVoiceSettingsResponse>> {
        wordsService.setDefaultVoiceForLocale(request.locale, request.voice)
        return ResponseEntity.ok().body(BrnResponse(data = buildVoiceSettingsResponse(request.locale)))
    }

    private fun buildVoiceSettingsResponse(locale: String): AudioVoiceSettingsResponse {
        val defaultVoice = wordsService.getDefaultVoiceForLocale(locale)
        val voiceOptions =
            wordsService.getAvailableVoicesForLocale(locale).map { voice ->
                AudioVoiceOptionResponse(
                    name = voice.name,
                    apiValue = voice.apiValue,
                    gender = voice.gender.name.lowercase(),
                    roles = voice.supportedRoles.map { it.apiValue },
                    isDefault = voice.name == defaultVoice,
                )
            }

        return AudioVoiceSettingsResponse(
            locale = locale.lowercase(),
            defaultVoice = defaultVoice,
            voices = voiceOptions,
        )
    }
}
