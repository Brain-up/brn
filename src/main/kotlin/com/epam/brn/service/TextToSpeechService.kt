package com.epam.brn.service

import java.io.File
import java.io.InputStream

interface TextToSpeechService {
    fun generateAudioOggFile(audioFileMetaData: AudioFileMetaData): File
    fun generateAudioOggFileWithValidation(
        text: String,
        locale: String,
        voice: String,
        speed: String,
        gender: String? = null,
        pitch: String? = null,
        style: String? = null
    ): InputStream
}
