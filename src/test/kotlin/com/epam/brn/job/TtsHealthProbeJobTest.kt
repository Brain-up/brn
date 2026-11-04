package com.epam.brn.job

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.exception.YandexServiceException
import com.epam.brn.service.TextToSpeechService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.InputStream

@ExtendWith(MockKExtension::class)
internal class TtsHealthProbeJobTest {
    @InjectMockKs
    lateinit var ttsHealthProbeJob: TtsHealthProbeJob

    @MockK
    lateinit var textToSpeechService: TextToSpeechService

    @Test
    fun `should synthesize a probe phrase through the active provider`() {
        // GIVEN
        val stream = mockk<InputStream>(relaxed = true)
        every { textToSpeechService.generateAudioOggStreamWithValidation(any()) } returns stream

        // WHEN
        ttsHealthProbeJob.probe()

        // THEN
        verify { textToSpeechService.generateAudioOggStreamWithValidation(any<AudioFileMetaData>()) }
        verify { stream.close() }
    }

    @Test
    fun `should swallow a provider failure so the scheduler keeps running`() {
        // GIVEN
        every {
            textToSpeechService.generateAudioOggStreamWithValidation(any())
        } throws YandexServiceException("down")

        // WHEN / THEN (no exception propagates)
        ttsHealthProbeJob.probe()
    }
}
