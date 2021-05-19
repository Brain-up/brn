package com.epam.brn.controller

import com.epam.brn.dto.AudiometryDto
import com.epam.brn.enums.AudiometryType
import com.epam.brn.service.AudiometryService
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)

internal class AudiometryControllerTest {

    @InjectMocks
    lateinit var audiometryController: AudiometryController

    @Mock
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
        `when`(audiometryService.getAudiometrics(locale)).thenReturn(listOf(audiometryDto))

        val audiometrics = audiometryController.getAudiometrics(locale)

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

        `when`(audiometryService.getAudiometry(audiometryId)).thenReturn(audiometryDto)

        val audiometry = audiometryController.getAudiometry(audiometryId)

        assertEquals(HttpStatus.SC_OK, audiometry.statusCode.value())

        assertEquals(audiometryDto, audiometry.body!!.data)
    }
}
