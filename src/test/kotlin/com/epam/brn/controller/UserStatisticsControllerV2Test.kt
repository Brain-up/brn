package com.epam.brn.controller

import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.statistics.DayStudyStatistics
import com.epam.brn.dto.statistics.MonthStudyStatistics
import com.epam.brn.dto.statistics.UserDailyDetailStatisticsDto
import com.epam.brn.service.RoleService
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.statistics.UserPeriodStatisticsService
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
internal class UserStatisticsControllerV2Test {
    @InjectMockKs
    private lateinit var userStatisticControllerV2: UserStatisticControllerV2

    @MockK
    private lateinit var userDayStatisticService: UserPeriodStatisticsService<DayStudyStatistics>

    @MockK
    private lateinit var userMonthStatisticService: UserPeriodStatisticsService<MonthStudyStatistics>

    @MockK
    private lateinit var studyHistoryService: StudyHistoryService

    @MockK
    private lateinit var roleService: RoleService

    @Test
    fun `should get user weekly statistics`() {
        // GIVEN
        val dayStudyStatistics = mockk<DayStudyStatistics>()
        val from = LocalDateTime.of(2021, 5, 1, 0, 0)
        val to = LocalDateTime.of(2021, 6, 5, 23, 59)

        val dayStudyStatisticList = listOf(dayStudyStatistics)

        // WHEN
        every { userDayStatisticService.getStatisticsForPeriod(from, to) } returns dayStudyStatisticList
        val userWeeklyStatistic = userStatisticControllerV2.getUserWeeklyStatistics(from, to, null)

        // THEN
        verify(exactly = 1) { userDayStatisticService.getStatisticsForPeriod(from, to) }
        assertEquals(HttpStatus.SC_OK, userWeeklyStatistic.statusCodeValue)
        assertEquals(dayStudyStatisticList, (userWeeklyStatistic.body as BrnResponse).data)
    }

    @Test
    fun `should get user yearly statistics`() {
        // GIVEN
        val monthStudyStatistics = mockk<MonthStudyStatistics>()
        val from = LocalDateTime.of(2021, 5, 1, 0, 0)
        val to = LocalDateTime.of(2021, 6, 5, 23, 59)

        val monthStudyStatisticList = listOf(monthStudyStatistics)

        // WHEN
        every { userMonthStatisticService.getStatisticsForPeriod(from, to) } returns monthStudyStatisticList
        val userYearlyStatistic = userStatisticControllerV2.getUserYearlyStatistics(from, to, null)

        // THEN
        verify(exactly = 1) { userMonthStatisticService.getStatisticsForPeriod(from, to) }
        assertEquals(HttpStatus.SC_OK, userYearlyStatistic.statusCodeValue)
        assertEquals(monthStudyStatisticList, (userYearlyStatistic.body as BrnResponse).data)
    }

    @Test
    fun `getUserWeeklyStatistic should return daily details statistics`() {
        // GIVEN
        val date = LocalDateTime.now()
        val userDailyDetailStatisticsDto = mockk<UserDailyDetailStatisticsDto>()
        every { studyHistoryService.getUserDailyStatistics(date, null) } returns listOf(userDailyDetailStatisticsDto)

        // WHEN
        val userWeeklyStatistic = userStatisticControllerV2.getUserDailyDetailsStatistics(date, null)

        // THEN
        verify(exactly = 1) { studyHistoryService.getUserDailyStatistics(date, null) }
        userWeeklyStatistic.statusCodeValue shouldBe HttpStatus.SC_OK
        userWeeklyStatistic.body!!.data shouldBe listOf(userDailyDetailStatisticsDto)
    }

    @Test
    fun `getUserWeeklyStatistic should return weekly statistics for admin`() {
        // GIVEN
        val userId = 1L
        val date = LocalDateTime.now()
        val dayStudyStatistics = mockk<DayStudyStatistics>()
        every { userDayStatisticService.getStatisticsForPeriod(date, date, userId) } returns listOf(dayStudyStatistics)
        every { roleService.isCurrentUserAdmin() } returns true

        // WHEN
        val userWeeklyStatistic = userStatisticControllerV2.getUserWeeklyStatistics(date, date, userId)

        // THEN
        verify(exactly = 1) { userDayStatisticService.getStatisticsForPeriod(date, date, userId) }
        userWeeklyStatistic.statusCodeValue shouldBe HttpStatus.SC_OK
        userWeeklyStatistic.body!!.data shouldBe listOf(dayStudyStatistics)
    }

    @Test
    fun `getUserYearlyStatistic should return yearly statistics for admin`() {
        // GIVEN
        val userId = 1L
        val date = LocalDateTime.now()
        val monthStudyStatistics = mockk<MonthStudyStatistics>()
        every {
            userMonthStatisticService.getStatisticsForPeriod(
                date,
                date,
                userId,
            )
        } returns listOf(monthStudyStatistics)
        every { roleService.isCurrentUserAdmin() } returns true

        // WHEN
        val userYearlyStatistic = userStatisticControllerV2.getUserYearlyStatistics(date, date, userId)

        // THEN
        verify(exactly = 1) { userMonthStatisticService.getStatisticsForPeriod(date, date, userId) }
        userYearlyStatistic.statusCodeValue shouldBe HttpStatus.SC_OK
        userYearlyStatistic.body!!.data shouldBe listOf(monthStudyStatistics)
    }

    @Test
    fun `getUserWeeklyStatistic should return daily details statistics for admin`() {
        // GIVEN
        val userId = 1L
        val date = LocalDateTime.now()
        val userDailyDetailStatisticsDto = mockk<UserDailyDetailStatisticsDto>()
        every { studyHistoryService.getUserDailyStatistics(date, userId) } returns listOf(userDailyDetailStatisticsDto)
        every { roleService.isCurrentUserAdmin() } returns true

        // WHEN
        val userWeeklyStatistic = userStatisticControllerV2.getUserDailyDetailsStatistics(date, userId)

        // THEN
        verify(exactly = 1) { studyHistoryService.getUserDailyStatistics(date, userId) }
        userWeeklyStatistic.statusCodeValue shouldBe HttpStatus.SC_OK
        userWeeklyStatistic.body!!.data shouldBe listOf(userDailyDetailStatisticsDto)
    }
}
