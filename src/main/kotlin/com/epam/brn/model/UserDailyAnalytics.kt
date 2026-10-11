package com.epam.brn.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * A precomputed per-day delta of a user's study analytics (filled by [com.epam.brn.job.UserDailyAnalyticsJob]).
 * One row per user/role/[day] holding that day's activity only; period figures are
 * SUM/COUNT roll-ups over these rows for trend reporting.
 */
@Entity
@Table(
    indexes = [
        Index(name = "user_daily_analytics_ix_day", columnList = "day"),
        Index(name = "user_daily_analytics_ix_role_name", columnList = "role_name"),
    ],
)
class UserDailyAnalytics(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "day")
    val day: LocalDate,
    val userId: Long,
    @Column(name = "role_name")
    val roleName: String,
    val firstDone: LocalDateTime?,
    val lastDone: LocalDateTime?,
    val spentTime: Long?,
    val doneExercises: Int?,
) {
    override fun toString(): String = "UserDailyAnalytics(id=$id, day=$day, userId=$userId, roleName='$roleName', " +
        "firstDone=$firstDone, lastDone=$lastDone, spentTime=$spentTime, doneExercises=$doneExercises)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserDailyAnalytics

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (day != other.day) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + userId.hashCode()
        result = 31 * result + day.hashCode()
        return result
    }
}
