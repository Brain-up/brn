package com.epam.brn.controller

import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.dto.statistic.UserDailyDetailStatisticsDto
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
import java.time.LocalDateTime
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@DisplayName("UserStatisticControllerV2 test using MockK")
internal class UserStatisticControllerV2Test {

    @InjectMockKs
    private lateinit var userStatisticControllerV2: UserStatisticControllerV2

    @MockK
    private lateinit var userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>

    @MockK
    private lateinit var userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>

    @MockK
    private lateinit var studyHistoryService: StudyHistoryService

    @Test
    fun `should get user weekly statistic`() {
        // GIVEN
        val dayStudyStatistic = mockk<DayStudyStatistic>()
        val from = LocalDateTime.of(2021, 5, 1, 0, 0)
        val to = LocalDateTime.of(2021, 6, 5, 23, 59)

        val dayStudyStatisticList = listOf(dayStudyStatistic)

        // WHEN
        every { userDayStatisticService.getStatisticForPeriod(from, to) } returns dayStudyStatisticList
        val userWeeklyStatistic = userStatisticControllerV2.getUserWeeklyStatistic(from, to)

        // THEN
        verify(exactly = 1) { userDayStatisticService.getStatisticForPeriod(from, to) }
        assertEquals(HttpStatus.SC_OK, userWeeklyStatistic.statusCodeValue)
        assertEquals(dayStudyStatisticList, (userWeeklyStatistic.body as BaseResponse).data)
    }

    @Test
    fun `should get user yearly statistic`() {
        // GIVEN
        val monthStudyStatistic = mockk<MonthStudyStatistic>()
        val from = LocalDateTime.of(2021, 5, 1, 0, 0)
        val to = LocalDateTime.of(2021, 6, 5, 23, 59)

        val monthStudyStatisticList = listOf(monthStudyStatistic)

        // WHEN
        every { userMonthStatisticService.getStatisticForPeriod(from, to) } returns monthStudyStatisticList
        val userYearlyStatistic = userStatisticControllerV2.getUserYearlyStatistic(from, to)

        // THEN
        verify(exactly = 1) { userMonthStatisticService.getStatisticForPeriod(from, to) }
        assertEquals(HttpStatus.SC_OK, userYearlyStatistic.statusCodeValue)
        assertEquals(monthStudyStatisticList, (userYearlyStatistic.body as BaseResponse).data)
    }

    @Test
    fun `getUserWeeklyStatistic should return daily details statistic`() {
        // GIVEN
        val date = LocalDateTime.now()
        val userDailyDetailStatisticsDto = mockk<UserDailyDetailStatisticsDto>()
        every { studyHistoryService.getUserDailyStatistics(date, null) } returns listOf(userDailyDetailStatisticsDto)

        // WHEN
        val userWeeklyStatistic = userStatisticControllerV2.getUserDailyDetailsStatistics(date)

        // THEN
        verify(exactly = 1) { studyHistoryService.getUserDailyStatistics(date, null) }
        userWeeklyStatistic.statusCodeValue shouldBe HttpStatus.SC_OK
        userWeeklyStatistic.body!!.data shouldBe listOf(userDailyDetailStatisticsDto)
    }
}
