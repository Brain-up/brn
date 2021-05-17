package com.epam.brn.controller

import com.epam.brn.service.YandexSpeechKitService
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.io.InputStream
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals

@Suppress("EqualsBetweenInconvertibleTypes")
@ExtendWith(MockitoExtension::class)

internal class AudioControllerTest {

    @InjectMocks
    lateinit var controller: AudioController

    @Mock
    private lateinit var yandex: YandexSpeechKitService

    @Test
    fun `should get audio byte array`() {

        // GIVEN
        val text = "Testing_text"

        val locale = "locale"

        val stream: InputStream = ByteArrayInputStream(byteArrayOf(10, 20, 30, 40, 50))

        `when`(yandex.generateAudioOggFileWithValidation(text, locale)).thenReturn(stream)

        val audioByteArray = controller.getAudioByteArray(text, locale)

        assertEquals(HttpStatus.SC_OK, audioByteArray.statusCode.value())

        verify(yandex, times(1)).generateAudioOggFileWithValidation(text, locale)
    }
}
