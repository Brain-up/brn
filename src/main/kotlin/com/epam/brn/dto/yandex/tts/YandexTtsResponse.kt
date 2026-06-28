package com.epam.brn.dto.yandex.tts

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class YandexTtsResponse(
    val result: YandexTtsResult? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class YandexTtsResult(
    val audioChunk: AudioChunk? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AudioChunk(
    val data: String? = null,
)
