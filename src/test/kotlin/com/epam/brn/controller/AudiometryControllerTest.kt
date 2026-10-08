package com.epam.brn.controller

import com.epam.brn.dto.response.AudiometryResponse
import com.epam.brn.enums.AudiometryType
import com.epam.brn.service.AudiometryService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

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

        val audiometryResponse =
            AudiometryResponse(
                locale = "ru-ru",
                id = 1,
                name = "testName",
                description = "description",
                audiometryTasks = "any",
                audiometryType = AudiometryType.valueOf("SIGNALS"),
            )
        every { audiometryService.getAudiometrics(locale) } returns (listOf(audiometryResponse))

        // WHEN
        val audiometrics = audiometryController.getAudiometrics(locale)

        // THEN
        audiometrics.statusCode.value() shouldBe HttpStatus.SC_OK
        audiometrics.body!!.data shouldBe listOf(audiometryResponse)
    }

    @Test
    fun `should get audiometry`() {
        // GIVEN
        val audiometryId = 1L

        val audiometryResponse =
            AudiometryResponse(
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
        audiometry.statusCode.value() shouldBe HttpStatus.SC_OK
        audiometry.body!!.data shouldBe audiometryResponse
        verify(exactly = 1) { audiometryController.getAudiometry(audiometryId) }
    }
}
