package com.epam.brn.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class GenerationAudioFilesServiceTest {
    @InjectMocks
    lateinit var generationAudioFilesService: GenerationAudioFilesService

    @Test
    fun test() {
        generationAudioFilesService.init("kevin16")
        // high quality
        generationAudioFilesService.doSpeak("Hello world from Real's How To")
        generationAudioFilesService.doSpeak("babushka")
        generationAudioFilesService.terminate()
    }
}
