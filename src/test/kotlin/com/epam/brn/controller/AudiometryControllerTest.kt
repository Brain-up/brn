package com.epam.brn.controller

import com.epam.brn.dto.AudiometryResponse
import com.epam.brn.enums.AudiometryType
import com.epam.brn.service.AudiometryService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)

internal class AudiometryControllerTest {

    @InjectMockKs
    lateinit var audiometryController: AudiometryController

    @MockK
    private lateinit var audiometryService: AudiometryService

    @Test
    fun `should get audio metrics`() {

        // GIVEN
        val locale = "locale"

        val audiometryResponse = AudiometryResponse(
            locale = "ru-ru",
            id = 1,
            name = "testName",
            description = "description",
            audiometryTasks = "any",
            audiometryType = AudiometryType.valueOf("SIGNALS"),
        )
        every { audiometryService.getAudiometrics(locale) } returns(listOf(audiometryResponse))

        // WHEN
        val audiometrics = audiometryController.getAudiometrics(locale)

        // THEN
        assertEquals(HttpStatus.SC_OK, audiometrics.statusCode.value())
        assertEquals(listOf(audiometryResponse), audiometrics.body!!.data)
    }
    @Test
    fun `should get audiometry`() {
        // GIVEN
        val audiometryId = 1L

        val audiometryResponse = AudiometryResponse(
            locale = "ru-ru",
            id = 1,
            name = "testName",
            description = "description",
            audiometryTasks = "any",
            audiometryType = AudiometryType.valueOf("SIGNALS"),
        )

        every { audiometryService.getAudiometry(audiometryId) } returns audiometryResponse

        // WHEN
        val audiometry = audiometryController.getAudiometry(audiometryId)

        // THEN
        assertEquals(HttpStatus.SC_OK, audiometry.statusCode.value())
        assertEquals(audiometryResponse, audiometry.body!!.data)
        verify(exactly = 1) { audiometryController.getAudiometry(audiometryId) }
    }
}
