package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.response.SubGroupStatisticDto
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.service.statistic.UserStatisticService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@DisplayName("UserStatisticController test using MockK")

internal class UserStatisticControllerTest {

    @InjectMockKs
    private lateinit var userStatisticController: UserStatisticController

    @MockK
    private lateinit var userStatisticService: UserStatisticService<SubGroupStatisticDto>

    @MockK
    private lateinit var userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>

    @MockK
    private lateinit var userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>

    @MockK
    private lateinit var subGroupStatisticDto: SubGroupStatisticDto

    @MockK
    private lateinit var dayStudyStatistic: DayStudyStatistic

    @MockK
    private lateinit var monthStudyStatistic: MonthStudyStatistic

    @Test
    fun `should get user sub group statistic`() {

        // GIVEN

        val subGroupStatisticDtoList = listOf(subGroupStatisticDto)
        val ids = listOf(1L, 2L, 3L)

        // WHEN
        every { userStatisticService.getSubGroupStatistic(ids) } returns subGroupStatisticDtoList
        val userSubGroupStatistic = userStatisticController.getUserSubGroupStatistic(ids)

        // THEN
        verify(exactly = 1) { userStatisticService.getSubGroupStatistic(ids) }
        assertEquals(HttpStatus.SC_OK, userSubGroupStatistic.statusCodeValue)
        assertEquals(subGroupStatisticDtoList, (userSubGroupStatistic.body as BaseResponseDto).data)
    }

    @Test
    fun `should get user weekly statistic`() {

        // GIVEN
        val from = LocalDateTime.of(2021, 5, 1, 0, 0)
        val to = LocalDateTime.of(2021, 6, 5, 23, 59)

        val dayStudyStatisticList = listOf(dayStudyStatistic)

        // WHEN
        every { userDayStatisticService.getStatisticForPeriod(from, to) } returns dayStudyStatisticList
        val userWeeklyStatistic = userStatisticController.getUserWeeklyStatistic(from, to)

        // THEN
        verify(exactly = 1) { userDayStatisticService.getStatisticForPeriod(from, to) }
        assertEquals(HttpStatus.SC_OK, userWeeklyStatistic.statusCodeValue)
        assertEquals(dayStudyStatisticList, (userWeeklyStatistic.body as BaseSingleObjectResponseDto).data)
    }

    @Test
    fun `should get user yearly statistic`() {

        // GIVEN
        val from = LocalDateTime.of(2021, 5, 1, 0, 0)
        val to = LocalDateTime.of(2021, 6, 5, 23, 59)

        val monthStudyStatisticList = listOf(monthStudyStatistic)

        // WHEN
        every { userMonthStatisticService.getStatisticForPeriod(from, to) } returns monthStudyStatisticList
        val userYearlyStatistic = userStatisticController.getUserYearlyStatistic(from, to)

        // THEN
        verify(exactly = 1) { userMonthStatisticService.getStatisticForPeriod(from, to) }
        assertEquals(HttpStatus.SC_OK, userYearlyStatistic.statusCodeValue)
        assertEquals(monthStudyStatisticList, (userYearlyStatistic.body as BaseSingleObjectResponseDto).data)
    }
}
