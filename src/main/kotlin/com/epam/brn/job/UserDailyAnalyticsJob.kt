package com.epam.brn.job

import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Fills per-user per-day study-analytics deltas into the user_daily_analytics table.
 *
 * Runs at midnight and finalizes the day that has just ended (current_date - 1) — the last fully
 * completed day. Each row holds a single day's activity (a delta), so weekly/monthly/yearly figures
 * are plain SUM/COUNT roll-ups over this table. Runs only when `brn.user.analytics.job.enabled=true`.
 * It does NOT back the live /admin/users endpoint (that stays realtime, see #2971) — it builds a
 * day-by-day history for non-realtime analytics / trend reporting. Re-running for the same day is
 * idempotent: that day's rows are recomputed from study_history and replaced.
 */
@Component
@ConditionalOnProperty(name = ["brn.user.analytics.job.enabled"], havingValue = "true")
class UserDailyAnalyticsJob(
    private val jdbcTemplate: JdbcTemplate,
) {
    private val log = logger()

    @Scheduled(cron = "@midnight")
    @Transactional
    fun fillUserDailyAnalytics() {
        log.info("start filling user daily analytics...")
        jdbcTemplate.update(DELETE_PREVIOUS_DAY_SQL)
        val rowsCount = jdbcTemplate.update(INSERT_PREVIOUS_DAY_DELTA_SQL)
        log.info("user daily analytics filled successfully, $rowsCount rows inserted")
    }
}

private const val DELETE_PREVIOUS_DAY_SQL = "DELETE FROM user_daily_analytics WHERE day = current_date - 1;"

private const val INSERT_PREVIOUS_DAY_DELTA_SQL = """
    INSERT INTO user_daily_analytics (day, user_id, role_name, first_done, last_done,
                                spent_time, done_exercises)
    SELECT current_date - 1,
           s.user_id,
           r.name,
           min(s.start_time),
           max(s.start_time),
           coalesce(sum(s.spent_time_in_seconds), 0),
           count(distinct s.exercise_id)
    FROM study_history s,
         user_roles ur,
         role r
    WHERE s.user_id = ur.user_id
      AND ur.role_id = r.id
      AND s.start_time >= current_date - 1
      AND s.start_time < current_date
    GROUP BY s.user_id, r.name;
"""
