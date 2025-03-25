package com.epam.brn.service

import com.epam.brn.dto.AudioFileMetaData
import java.io.InputStream

interface TextToSpeechService {
    fun generateAudioOggStreamWithValidation(audioFileMetaData: AudioFileMetaData): InputStream
}
