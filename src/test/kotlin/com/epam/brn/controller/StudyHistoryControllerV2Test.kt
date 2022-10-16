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
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
internal class StudyHistoryControllerV2Test {

    @InjectMockKs
    lateinit var studyHistoryControllerV2: StudyHistoryControllerV2

    @MockK
    lateinit var studyHistoryService: StudyHistoryService

    @MockK
    lateinit var authorityService: AuthorityService

    @Test
    fun `getHistories should return histories`() {
        // GIVEN
        val userId = 1L
        val date = LocalDateTime.now()
        val studyHistoryDto = mockk<StudyHistoryDto>()
        every { studyHistoryService.getHistories(userId, date, date) } returns listOf(studyHistoryDto)
        every { authorityService.isCurrentUserAdmin() } returns true

        // WHEN
        val histories = studyHistoryControllerV2.getHistories(userId, date, date)

        // THEN
        verify(exactly = 1) { studyHistoryService.getHistories(userId, date, date) }
        histories.statusCodeValue shouldBe HttpStatus.SC_OK
        histories.body!!.data shouldBe listOf(studyHistoryDto)
    }
}
