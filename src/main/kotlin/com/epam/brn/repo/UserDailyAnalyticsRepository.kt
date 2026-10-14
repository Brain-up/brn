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
     * Atomically folds a single completed exercise into that user's per-day delta, for every role the
     * user holds (one row per user/role/day, matching the nightly reconcile grain). Inserts the day row
     * on first activity, otherwise increments the counts and widens [first_done]/[last_done]. A single
     * `ON CONFLICT` statement so concurrent completions for the same user/day cannot lose updates.
     *
     * [doneExercises] always grows by 1 (every completion counts, repeats included). [uniqueDoneExercises]
     * grows by [uniqueIncrement] (1 only when this is the first completion of this exercise that day, else
     * 0) — the caller decides, since distinctness can't be derived from the upsert alone; the nightly
     * reconcile recomputes it from raw history regardless.
     */
    @Modifying
    @Transactional
    @Query(
        value = """
            INSERT INTO user_daily_analytics (user_id, role_name, day, done_exercises, unique_done_exercises,
                                              spent_time, execution_time, first_done, last_done)
            SELECT :userId, r.name, :day, 1, :uniqueIncrement, :spentSeconds, :executionSeconds, :activityTime, :activityTime
            FROM user_roles ur
            JOIN role r ON ur.role_id = r.id
            WHERE ur.user_id = :userId
            ON CONFLICT (user_id, role_name, day)
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
