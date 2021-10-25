package com.epam.brn.service

import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.enums.Role.ROLE_ADMIN
import com.epam.brn.service.impl.UserAccountServiceImpl
import com.epam.brn.service.impl.UserAnalyticsServiceImpl
import com.epam.brn.service.statistic.UserPeriodStatisticService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import java.time.LocalDateTime
import java.time.Month
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Pageable

@ExtendWith(MockKExtension::class)
@DisplayName("UserAnalyticsService test using MockK")
internal class UserAnalyticsServiceTest {

    @InjectMockKs
    lateinit var userAnalyticsService: UserAnalyticsServiceImpl

    @MockK
    lateinit var userAccountService: UserAccountServiceImpl

    @MockK
    lateinit var userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>

    @MockK
    lateinit var pageable: Pageable

    @MockK(relaxed = true)
    lateinit var userWithAnalytics: UserWithAnalyticsResponse

    @MockK(relaxed = true)
    lateinit var dayStudyStatistic: DayStudyStatistic

    @Test
    fun `should return all users with analytics`() {

        val usersList = listOf(userWithAnalytics, userWithAnalytics)
        val dayStatisticList = listOf(dayStudyStatistic, dayStudyStatistic)

        every { userAccountService.getUsersWithAnalytics(pageable, ROLE_ADMIN.name) } returns usersList
        every { userDayStatisticService.getStatisticForPeriod(any(), any(), any()) } returns dayStatisticList

        val userAnalyticsDtos = userAccountService.getUsersWithAnalytics(pageable, ROLE_ADMIN.name)

        userAnalyticsDtos.size shouldBe 2
    }

    @Test
    fun `should not return user with analytics`() {

        val usersList = listOf(userWithAnalytics)
        val dayStatisticList = listOf(dayStudyStatistic, dayStudyStatistic)
        val from = LocalDateTime.of(2015, Month.JANUARY, 3, 0, 0, 0)

        every { userAccountService.getUsersWithAnalytics(pageable, ROLE_ADMIN.name) } returns usersList
        every { userDayStatisticService.getStatisticForPeriod(from, from, userWithAnalytics.id) } returns dayStatisticList

        val userAnalyticsDtos = userAccountService.getUsersWithAnalytics(pageable, ROLE_ADMIN.name)

        usersList.size shouldBe 1
        userAnalyticsDtos[0].lastWeek.size shouldBe 0
    }
}
