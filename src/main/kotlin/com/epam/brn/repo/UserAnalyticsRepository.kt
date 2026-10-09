package com.epam.brn.repo

import com.epam.brn.model.UserAnalytics
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface UserAnalyticsRepository : JpaRepository<UserAnalytics, Long> {
    fun findBySnapshotDate(snapshotDate: LocalDate): List<UserAnalytics>

    fun findByUserIdOrderBySnapshotDate(userId: Long): List<UserAnalytics>
}
