package com.epam.brn.job

import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Appends a daily snapshot of per-user study analytics into the user_analytics table.
 *
 * Runs only when `brn.user.analytics.job.enabled=true` (off by default). It does NOT back the
 * live /admin/users endpoint (that stays realtime, see #2971) — it builds a day-by-day history
 * for non-realtime analytics / trend reporting. Re-running on the same day is idempotent: today's
 * rows are replaced.
 */
@Component
@ConditionalOnProperty(name = ["brn.user.analytics.job.enabled"], havingValue = "true")
class UserAnalyticsJob(
    private val jdbcTemplate: JdbcTemplate,
) {
    private val log = logger()

    @Scheduled(cron = "@midnight")
    @Transactional
    fun fillUserAnalytics() {
        try {
            log.info("start filling user analytics snapshot...")
            jdbcTemplate.update(DELETE_TODAY_SNAPSHOT_SQL)
            val rowsCount = jdbcTemplate.update(INSERT_TODAY_SNAPSHOT_SQL)
            log.info("user analytics snapshot filled successfully, $rowsCount rows inserted")
        } catch (e: Exception) {
            log.error("Failed to fill user analytics snapshot: ${e.message}", e)
        }
    }
}

private const val DELETE_TODAY_SNAPSHOT_SQL = "DELETE FROM user_analytics WHERE snapshot_date = current_date;"

private const val INSERT_TODAY_SNAPSHOT_SQL = """
    INSERT INTO user_analytics (snapshot_date, user_id, role_name, first_done, last_done,
                                spent_time, done_exercises, study_days)
    SELECT current_date,
           s.user_id,
           r.name,
           min(s.start_time),
           max(s.start_time),
           coalesce(sum(s.spent_time_in_seconds), 0),
           count(distinct s.exercise_id),
           (SELECT count(distinct date_trunc('day', s1.start_time))
            FROM study_history s1
            WHERE s1.user_id = s.user_id
              AND s1.start_time BETWEEN date_trunc('month', current_date) AND current_timestamp)
    FROM study_history s,
         user_roles ur,
         role r
    WHERE s.user_id = ur.user_id
      AND ur.role_id = r.id
    GROUP BY s.user_id, r.name;
"""
