package com.epam.brn.job

import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Reconciles per-user per-day study-analytics deltas in the user_daily_analytics table against the
 * raw study_history.
 *
 * Runs at midnight over a trailing window of recently-finalized days — `[current_date - reconcileDays,
 * current_date)` — recomputing each day's delta from study_history and overwriting the stored rows, so
 * any drift from missed or duplicated incremental updates self-heals (not just yesterday's). The current
 * day is left untouched; it is kept fresh incrementally and finalized by the next night's run. Each row
 * holds a single day's activity (a delta), so weekly/monthly/yearly figures are plain SUM/COUNT roll-ups.
 * Runs only when `brn.user.analytics.precompute.enabled=true`. It does NOT back the live /admin/users
 * endpoint (that stays realtime, see #2971). Re-running is idempotent: the window's rows are deleted and
 * recomputed from study_history, so any number of runs yields the same result.
 */
@Component
@ConditionalOnProperty(name = ["brn.user.analytics.precompute.enabled"], havingValue = "true")
class UserDailyAnalyticsJob(
    private val jdbcTemplate: JdbcTemplate,
    @Value("\${brn.user.analytics.precompute.reconcile-days:7}") private val reconcileDays: Int,
) {
    private val log = logger()

    @Scheduled(cron = "@midnight")
    @Transactional
    fun fillUserDailyAnalytics() {
        log.info("start reconciling user daily analytics over the last $reconcileDays day(s)...")
        jdbcTemplate.update(DELETE_WINDOW_SQL, reconcileDays)
        val rowsCount = jdbcTemplate.update(INSERT_WINDOW_DELTA_SQL, reconcileDays)
        log.info("user daily analytics reconciled successfully, $rowsCount rows written")
    }
}

private const val DELETE_WINDOW_SQL =
    "DELETE FROM user_daily_analytics WHERE day >= current_date - cast(? as integer) AND day < current_date;"

private const val INSERT_WINDOW_DELTA_SQL = """
    INSERT INTO user_daily_analytics (day, user_id, first_done, last_done,
                                spent_time, execution_time, done_exercises, unique_done_exercises)
    SELECT cast(s.start_time as date),
           s.user_id,
           min(s.start_time),
           max(s.start_time),
           coalesce(sum(s.spent_time_in_seconds), 0),
           coalesce(sum(s.execution_seconds), 0),
           count(*),
           count(distinct s.exercise_id)
    FROM study_history s
    WHERE s.start_time >= current_date - cast(? as integer)
      AND s.start_time < current_date
    GROUP BY cast(s.start_time as date), s.user_id;
"""
