package com.epam.brn.repo

import com.epam.brn.model.UserDailyAnalytics
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface UserDailyAnalyticsRepository : JpaRepository<UserDailyAnalytics, Long> {
    fun findByDay(day: LocalDate): List<UserDailyAnalytics>

    fun findByUserIdOrderByDay(userId: Long): List<UserDailyAnalytics>
}
