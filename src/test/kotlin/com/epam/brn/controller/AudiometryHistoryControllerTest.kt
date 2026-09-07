package com.epam.brn.controller

import com.epam.brn.dto.request.AudiometryHistoryRequest
import com.epam.brn.service.AudiometryHistoryService
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
internal class AudiometryHistoryControllerTest {
    @InjectMockKs
    lateinit var audiometryHistoryController: AudiometryHistoryController

    @MockK
    private lateinit var audiometryHistoryService: AudiometryHistoryService

    @MockK
    private lateinit var audiometryHistory: AudiometryHistoryRequest

    @Test
    fun `should save speech audiometry history`() {
        // GIVEN
        val baseSingleObjectResponseDto = 1L
        every { audiometryHistoryService.save(audiometryHistory) } returns baseSingleObjectResponseDto

        // WHEN
        val save = audiometryHistoryController.save(audiometryHistory)

        // THEN
        save.statusCode.value() shouldBe HttpStatus.SC_OK
        save.body!!.data shouldBe baseSingleObjectResponseDto
        verify(exactly = 1) { audiometryHistoryService.save(audiometryHistory) }
    }
}
