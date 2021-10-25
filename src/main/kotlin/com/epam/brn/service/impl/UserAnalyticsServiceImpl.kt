package com.epam.brn.service.impl

import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.WeekFields
import java.util.Locale

@Service
class UserAnalyticsServiceImpl(
    private val userAccountService: UserAccountService,
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>
) : UserAnalyticsService {

    override fun getUsersWithAnalytics(pageable: Pageable, role: String): List<UserWithAnalyticsResponse> {
        val users = userAccountService.getUsersWithAnalytics(pageable, role)

        val now = LocalDate.now()
        val fieldISO = WeekFields.of(Locale.getDefault()).dayOfWeek()

        users.map { it.lastWeek = userDayStatisticService.getStatisticForPeriod(LocalDateTime.of(now.with(fieldISO, 1L), LocalTime.MIN), LocalDateTime.of(now.with(fieldISO, 1L).plusDays(7L), LocalTime.MAX), it.id) }

        return users
    }
}
