package com.epam.brn.dto.yandex.tts

import com.fasterxml.jackson.annotation.JsonInclude

data class YandexTtsRequest(
    val text: String,
    val outputAudioSpec: OutputAudioSpec,
    val hints: List<Hint>,
    val loudnessNormalizationType: String = "LUFS",
)

data class OutputAudioSpec(
    val containerAudio: ContainerAudio,
)

data class ContainerAudio(
    val containerAudioType: String = "OGG_OPUS",
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Hint(
    val voice: String? = null,
    val speed: String? = null,
    val role: String? = null,
)
