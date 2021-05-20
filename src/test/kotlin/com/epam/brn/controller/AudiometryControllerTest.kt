package com.epam.brn.controller

import com.epam.brn.dto.AudiometryDto
import com.epam.brn.enums.AudiometryType
import com.epam.brn.service.AudiometryService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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

        val audiometryDto = AudiometryDto(
            locale = "ru-ru",
            id = 1,
            name = "testName",
            description = "description",
            audiometryTasks = "any",
            audiometryType = AudiometryType.valueOf("SIGNALS"),
        )
        every { audiometryService.getAudiometrics(locale) } returns(listOf(audiometryDto))

        // WHEN
        val audiometrics = audiometryController.getAudiometrics(locale)

        // THEN
        assertEquals(HttpStatus.SC_OK, audiometrics.statusCode.value())
        assertEquals(listOf(audiometryDto), audiometrics.body!!.data)
    }
    @Test
    fun `should get audiometry`() {
        // GIVEN
        val audiometryId = 1L

        val audiometryDto = AudiometryDto(
            locale = "ru-ru",
            id = 1,
            name = "testName",
            description = "description",
            audiometryTasks = "any",
            audiometryType = AudiometryType.valueOf("SIGNALS"),
        )

        every { audiometryService.getAudiometry(audiometryId) } returns audiometryDto

        // WHEN
        val audiometry = audiometryController.getAudiometry(audiometryId)

        // THEN
        assertEquals(HttpStatus.SC_OK, audiometry.statusCode.value())
        assertEquals(audiometryDto, audiometry.body!!.data)
    }
}
