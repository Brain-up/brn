package com.epam.brn.controller

import com.epam.brn.cloud.CloudService
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
@DisplayName("AdminControllerTestV2 test using MockK")
class AdminControllerTestV2 {

    @InjectMockKs
    private lateinit var adminController: AdminControllerV2

    @MockK
    private lateinit var studyHistoryService: StudyHistoryService

    @MockK
    private lateinit var userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>

    @MockK
    private lateinit var userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>

    @MockK
    private lateinit var cloudService: CloudService

    @Test
    fun `getHistories should return histories`() {
        // GIVEN
        val userId = 1L
        val date = LocalDateTime.now()
        val studyHistoryDto = mockk<StudyHistoryDto>()
        every { studyHistoryService.getHistories(userId, date, date) } returns listOf(studyHistoryDto)

        // WHEN
        val histories = adminController.getHistories(userId, date, date)

        // THEN
        verify(exactly = 1) { studyHistoryService.getHistories(userId, date, date) }
        histories.statusCodeValue shouldBe HttpStatus.SC_OK
        histories.body!!.data shouldBe listOf(studyHistoryDto)
    }

    @Test
    fun `getUserWeeklyStatistic should return weekly statistic`() {
        // GIVEN
        val userId = 1L
        val date = LocalDateTime.now()
        val dayStudyStatistic = mockk<DayStudyStatistic>()
        every { userDayStatisticService.getStatisticForPeriod(date, date, userId) } returns listOf(dayStudyStatistic)

        // WHEN
        val userWeeklyStatistic = adminController.getUserWeeklyStatistic(date, date, userId)

        // THEN
        verify(exactly = 1) { userDayStatisticService.getStatisticForPeriod(date, date, userId) }
        userWeeklyStatistic.statusCodeValue shouldBe HttpStatus.SC_OK
        userWeeklyStatistic.body!!.data shouldBe listOf(dayStudyStatistic)
    }

    @Test
    fun `getUserYearlyStatistic should return yearly statistic`() {
        // GIVEN
        val userId = 1L
        val date = LocalDateTime.now()
        val monthStudyStatistic = mockk<MonthStudyStatistic>()
        every {
            userMonthStatisticService.getStatisticForPeriod(
                date,
                date,
                userId
            )
        } returns listOf(monthStudyStatistic)

        // WHEN
        val userYearlyStatistic = adminController.getUserYearlyStatistic(date, date, userId)

        // THEN
        verify(exactly = 1) { userMonthStatisticService.getStatisticForPeriod(date, date, userId) }
        userYearlyStatistic.statusCodeValue shouldBe HttpStatus.SC_OK
        userYearlyStatistic.body!!.data shouldBe listOf(monthStudyStatistic)
    }

    @Test
    fun `uploadFile should call cloud service upload file and return status OK`() {
        // GIVEN
        val path = "path/folder/"
        val fileName = "audio/ogg"
        val data = ByteArray(0)
        val multipartFile = MockMultipartFile("file.ogg", data)
        every { cloudService.uploadFile(path, fileName, multipartFile) } returns Unit

        // WHEN
        val response = adminController.upload(path, fileName, multipartFile)

        // THEN
        verify(exactly = 1) { cloudService.uploadFile(path, fileName, multipartFile) }
        assertEquals(HttpStatus.SC_CREATED, response.statusCode.value())
        assertNull(response.body)
    }
}
