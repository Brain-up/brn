package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.response.SubGroupStatisticResponse
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
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@DisplayName("UserStatisticController test using MockK")
internal class UserStatisticControllerTest {

    @InjectMockKs
    private lateinit var userStatisticController: UserStatisticController

    @MockK
    private lateinit var userStatisticService: UserStatisticService<SubGroupStatisticResponse>

    @MockK
    private lateinit var userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>

    @MockK
    private lateinit var userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>

    @MockK
    private lateinit var subGroupStatisticResponse: SubGroupStatisticResponse

    @Test
    fun `should get user sub group statistic`() {
        // GIVEN
        val subGroupStatisticDtoList = listOf(subGroupStatisticResponse)
        val ids = listOf(1L, 2L, 3L)

        // WHEN
        every { userStatisticService.getSubGroupStatistic(ids) } returns subGroupStatisticDtoList
        val userSubGroupStatistic = userStatisticController.getUserSubGroupStatistic(ids)

        // THEN
        verify(exactly = 1) { userStatisticService.getSubGroupStatistic(ids) }
        assertEquals(HttpStatus.SC_OK, userSubGroupStatistic.statusCodeValue)
        assertEquals(subGroupStatisticDtoList, (userSubGroupStatistic.body as BaseResponseDto).data)
    }
}
