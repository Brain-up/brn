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
 * One row per user/[day] holding that day's activity only; period figures are SUM/COUNT roll-ups over
 * these rows for trend reporting. A reader that needs a role cohort filters the user set by role and
 * aggregates by [userId] (study_history rows carry no role, and role here is just a user property).
 *
 * [spentTime] is in **seconds** (summed from `study_history.spent_time_in_seconds`, i.e. end-start).
 * [executionTime] is also in **seconds** (summed from `study_history.execution_seconds`, the active
 * execution time the week/month/year statistics graphs sum).
 * [doneExercises] counts **all** completions that day (repeats included); [uniqueDoneExercises] counts
 * the distinct exercises done that day.
 */
@Entity
@Table(
    indexes = [
        Index(name = "user_daily_analytics_ix_day", columnList = "day"),
    ],
)
class UserDailyAnalytics(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "day")
    val day: LocalDate,
    val userId: Long,
    val firstDone: LocalDateTime?,
    val lastDone: LocalDateTime?,
    val spentTime: Long?,
    @Column(name = "execution_time")
    val executionTime: Long?,
    val doneExercises: Int?,
    @Column(name = "unique_done_exercises")
    val uniqueDoneExercises: Int?,
) {
    override fun toString(): String = "UserDailyAnalytics(id=$id, day=$day, userId=$userId, " +
        "firstDone=$firstDone, lastDone=$lastDone, spentTime=$spentTime, executionTime=$executionTime, " +
        "doneExercises=$doneExercises, uniqueDoneExercises=$uniqueDoneExercises)"

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
