package com.epam.brn.dto.response.audio

data class AudioVoiceSettingsResponse(
    val locale: String,
    val defaultVoice: String,
    val voices: List<AudioVoiceOptionResponse>,
)

data class AudioVoiceOptionResponse(
    val name: String,
    val apiValue: String,
    val gender: String,
    val roles: List<String>,
    val isDefault: Boolean,
)
