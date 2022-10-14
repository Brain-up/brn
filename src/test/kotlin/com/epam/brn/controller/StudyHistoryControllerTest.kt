package com.epam.brn.controller

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.service.StudyHistoryService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class StudyHistoryControllerTest {

    @InjectMockKs
    lateinit var studyHistoryController: StudyHistoryController

    @MockK
    lateinit var studyHistoryService: StudyHistoryService

    @MockK
    lateinit var authorityService: AuthorityService

    @Test
    fun `should create new study history`() {
        // GIVEN
        val dto = StudyHistoryDto(
            id = 1L,
            exerciseId = 1L,
            startTime = LocalDateTime.now().minusMinutes(1),
            endTime = LocalDateTime.now(),
            executionSeconds = 60,
            tasksCount = 1,
            replaysCount = 4,
            wrongAnswers = 3
        )
        every { studyHistoryService.save(dto) } returns dto

        // WHEN
        val result = studyHistoryController.save(dto)

        // THEN
        verify(exactly = 1) { studyHistoryService.save(dto) }
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @Test
    fun `getMonthHistories should return month histories`() {
        // GIVEN
        val userId = 1L
        val month = 1
        val year = 2021
        val studyHistoryDto = mockk<StudyHistoryDto>()
        every { studyHistoryService.getMonthHistories(userId, month, year) } returns listOf(studyHistoryDto)
        every { authorityService.isCurrentUserAdmin() } returns true

        // WHEN
        val monthHistories = studyHistoryController.getMonthHistories(month, year, userId)

        // THEN
        verify(exactly = 1) { studyHistoryService.getMonthHistories(userId, month, year) }
        monthHistories.statusCodeValue shouldBe org.apache.http.HttpStatus.SC_OK
        monthHistories.body!!.data shouldBe listOf(studyHistoryDto)
    }
}
