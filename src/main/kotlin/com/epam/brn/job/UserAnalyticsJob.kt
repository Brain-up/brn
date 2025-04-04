package com.epam.brn.job

import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@ConditionalOnProperty(name = ["brn.user.analytics.job.enabled"], havingValue = "true")
class UserAnalyticsJob(
    private val jdbcTemplate: JdbcTemplate
) {
    private val log = logger()
    @Scheduled(cron = "@midnight")
    @Transactional
    fun fillUserAnalytics() {
        try {
            log.info("start filling study analytics table...")
            val rowsCount = jdbcTemplate.update(FILL_USER_ANALYTICS_SQL)
            log.info("filling study analytics table was finished successfully. total $rowsCount rows inserted")
        } catch (e: Exception) {
            log.error("Some error occurred on fill statistics tables: ${e.message}", e)
        }
    }
}

private const val FILL_USER_ANALYTICS_SQL: String = """        
           TRUNCATE TABLE user_analytics CASCADE;
           INSERT INTO user_analytics (user_id, first_done, last_done, spent_time, 
                                       done_exercises, study_days, role_name)
             SELECT s.user_id,
                   min(start_time),
                   max(start_time),
                   coalesce(sum(spent_time_in_seconds), 0),
                   count(distinct exercise_id),
                   (SELECT distinct count(distinct date_trunc('day', s1.start_time))
                    FROM study_history s1
                    WHERE s1.user_id = s.user_id
                      AND s1.start_time between date_trunc('month', current_date)
                      AND date_trunc('month', current_date) + interval '1 month - 1 microsecond'),
                    r.name            
            FROM study_history s,
                 user_roles ur,
                 role r
            WHERE s.user_id = ur.user_id
                  AND ur.role_id = r.id
            GROUP BY s.user_id, r.name;
        """
