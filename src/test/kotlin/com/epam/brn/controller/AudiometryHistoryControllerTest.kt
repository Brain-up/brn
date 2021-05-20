package com.epam.brn.controller

import com.epam.brn.dto.request.AudiometryHistoryRequest
import com.epam.brn.service.AudiometryHistoryService
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
        assertEquals(HttpStatus.SC_OK, save.statusCode.value())
        verify(exactly = 1) { audiometryHistoryService.save(audiometryHistory) }
    }
}
