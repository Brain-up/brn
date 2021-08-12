package com.epam.brn.controller

import com.epam.brn.service.YandexSpeechKitService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.extension.ExtendWith
import java.io.InputStream
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)

internal class AudioControllerTest {

    @InjectMockKs
    lateinit var controller: AudioController

    @MockK
    private lateinit var yandex: YandexSpeechKitService

    @Test
    fun `should get audio byte array`() {

        // GIVEN
        val text = "Testing_text"

        val locale = "locale"

        val stream: InputStream = ByteArrayInputStream(byteArrayOf(10, 20, 30, 40, 50))

        every { yandex.generateAudioOggFileWithValidation(text, locale) } returns stream

        // WHEN
        val audioByteArray = controller.getAudioByteArray(text, locale)

        // THEN
        assertEquals(HttpStatus.SC_OK, audioByteArray.statusCode.value())
        verify(exactly = 1) { yandex.generateAudioOggFileWithValidation(text, locale) }
    }
}
