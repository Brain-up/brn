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
 * Precomputed per-day study analytics, one row per user/[day]; periods are SUM/COUNT roll-ups.
 * [spentTime] and [executionTime] are in seconds. [doneExercises] counts all completions,
 * [uniqueDoneExercises] distinct exercises.
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
