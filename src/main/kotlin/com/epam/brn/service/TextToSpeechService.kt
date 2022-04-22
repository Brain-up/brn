package com.epam.brn.service

import com.epam.brn.dto.AudioFileMetaData
import java.io.File
import java.io.InputStream

interface TextToSpeechService {
    fun generateAudioOggFile(audioFileMetaData: AudioFileMetaData): File
    fun generateAudioOggFileWithValidation(audioFileMetaData: AudioFileMetaData): InputStream
}
