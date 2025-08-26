package com.epam.brn.controller

import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.response.SubGroupStatisticsResponse
import com.epam.brn.dto.statistics.DayStudyStatistics
import com.epam.brn.dto.statistics.MonthStudyStatistics
import com.epam.brn.service.statistics.UserPeriodStatisticsService
import com.epam.brn.service.statistics.UserStatisticService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@DisplayName("UserStatisticController test using MockK")
internal class UserSubGroupStatisticsControllerTest {
    @InjectMockKs
    private lateinit var userStatisticController: UserSubGroupStatisticController

    @MockK
    private lateinit var userStatisticService: UserStatisticService<SubGroupStatisticsResponse>

    @MockK
    private lateinit var userDayStatisticService: UserPeriodStatisticsService<DayStudyStatistics>

    @MockK
    private lateinit var userMonthStatisticService: UserPeriodStatisticsService<MonthStudyStatistics>

    @MockK
    private lateinit var subGroupStatisticResponse: SubGroupStatisticsResponse

    @Test
    fun `should get user sub group statistics`() {
        // GIVEN
        val subGroupStatisticDtoList = listOf(subGroupStatisticResponse)
        val ids = listOf(1L, 2L, 3L)

        // WHEN
        every { userStatisticService.getSubGroupStatistic(ids) } returns subGroupStatisticDtoList
        val userSubGroupStatistic = userStatisticController.getUserSubGroupStatistic(ids)

        // THEN
        verify(exactly = 1) { userStatisticService.getSubGroupStatistic(ids) }
        assertEquals(HttpStatus.SC_OK, userSubGroupStatistic.statusCodeValue)
        assertEquals(subGroupStatisticDtoList, (userSubGroupStatistic.body as BrnResponse).data)
    }
}
