package com.epam.brn.repo

import com.epam.brn.model.UserDailyAnalytics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

interface UserDailyAnalyticsRepository : JpaRepository<UserDailyAnalytics, Long> {
    fun findByDay(day: LocalDate): List<UserDailyAnalytics>

    fun findByUserIdOrderByDay(userId: Long): List<UserDailyAnalytics>

    /**
     * Atomic upsert (`ON CONFLICT`) of one completion into the user's day row.
     * done_exercises grows by 1; unique_done_exercises by [uniqueIncrement] (decided by the caller).
     */
    @Modifying
    @Transactional
    @Query(
        value = """
            INSERT INTO user_daily_analytics (user_id, day, done_exercises, unique_done_exercises,
                                              spent_time, execution_time, first_done, last_done)
            VALUES (:userId, :day, 1, :uniqueIncrement, :spentSeconds, :executionSeconds, :activityTime, :activityTime)
            ON CONFLICT (user_id, day)
            DO UPDATE SET done_exercises        = COALESCE(user_daily_analytics.done_exercises, 0) + 1,
                          unique_done_exercises = COALESCE(user_daily_analytics.unique_done_exercises, 0) + EXCLUDED.unique_done_exercises,
                          spent_time            = COALESCE(user_daily_analytics.spent_time, 0) + EXCLUDED.spent_time,
                          execution_time        = COALESCE(user_daily_analytics.execution_time, 0) + EXCLUDED.execution_time,
                          first_done            = LEAST(user_daily_analytics.first_done, EXCLUDED.first_done),
                          last_done             = GREATEST(user_daily_analytics.last_done, EXCLUDED.last_done)
        """,
        nativeQuery = true,
    )
    fun upsertDailyDelta(
        @Param("userId") userId: Long,
        @Param("day") day: LocalDate,
        @Param("spentSeconds") spentSeconds: Long,
        @Param("executionSeconds") executionSeconds: Long,
        @Param("uniqueIncrement") uniqueIncrement: Int,
        @Param("activityTime") activityTime: LocalDateTime,
    ): Int
}
