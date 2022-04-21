package com.epam.brn.controller

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.service.UserAnalyticsService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)

internal class AudioControllerTest {

    @InjectMockKs
    lateinit var controller: AudioController

    @MockK
    private lateinit var userAnalyticsService: UserAnalyticsService

    @Test
    fun `should get audio byte array by request with exerciseId `() {
        // GIVEN
        val text = "Testing_text"
        val locale = "locale"
        val stream: InputStream = ByteArrayInputStream(byteArrayOf(10, 20, 30, 40, 50))
        val audioFileMetaData = AudioFileMetaData(text, locale, "", "1", null, null, null)
        every { userAnalyticsService.prepareAudioFileForUser(1, audioFileMetaData) } returns stream

        // WHEN
        val audioByteArray = controller.getAudioByteArray(text, 1, locale, "", "1")

        // THEN
        assertEquals(HttpStatus.SC_OK, audioByteArray.statusCode.value())
        verify(exactly = 1) { userAnalyticsService.prepareAudioFileForUser(1, audioFileMetaData) }
    }
}
