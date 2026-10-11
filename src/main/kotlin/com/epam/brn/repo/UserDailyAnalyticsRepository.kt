package com.epam.brn.repo

import com.epam.brn.model.UserDailyAnalytics
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface UserDailyAnalyticsRepository : JpaRepository<UserDailyAnalytics, Long> {
    fun findBySnapshotDate(snapshotDate: LocalDate): List<UserDailyAnalytics>

    fun findByUserIdOrderBySnapshotDate(userId: Long): List<UserDailyAnalytics>
}
