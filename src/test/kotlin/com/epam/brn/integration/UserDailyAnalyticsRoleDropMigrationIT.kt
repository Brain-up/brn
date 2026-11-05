package com.epam.brn.integration

import io.kotest.matchers.shouldBe
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.sql.DriverManager

/**
 * Regression for the failed prod deploy of V220261013_2609: a DB already holding rows of the old
 * (user_id, role_name, day) grain — two rows per day for a multi-role user — must migrate cleanly.
 * Runs Flyway directly against its own fresh container, not the Spring context (which is already migrated).
 */
@Tag("integration-test")
class UserDailyAnalyticsRoleDropMigrationIT {
    private val url = "jdbc:tc:postgresql:16:///migration-2609-db?TC_DAEMON=true"

    @Test
    fun `should migrate old per-role rows of a multi-role user without unique-constraint violation`() {
        // GIVEN the schema as it is on prod (up to the old backfill) with a multi-role user's day
        flyway("220261011.2609").migrate()
        DriverManager.getConnection(url, "test", "test").use { conn ->
            conn.createStatement().use {
                it.executeUpdate(
                    """
                    insert into user_daily_analytics (day, user_id, role_name, spent_time, done_exercises)
                    values (current_date - 2, 1, 'USER', 10, 1),
                           (current_date - 2, 1, 'ADMIN', 10, 1)
                    """.trimIndent(),
                )
            }
        }

        // WHEN the remaining migrations are applied
        val result = flyway(null).migrate()

        // THEN the migration succeeds and the stale per-role rows are gone (study_history is empty)
        result.success shouldBe true
        DriverManager.getConnection(url, "test", "test").use { conn ->
            conn.createStatement().use { st ->
                st.executeQuery("select count(*) from user_daily_analytics").use { rs ->
                    rs.next()
                    rs.getInt(1) shouldBe 0
                }
            }
        }
    }

    private fun flyway(target: String?): Flyway = Flyway
        .configure()
        .dataSource(url, "test", "test")
        .locations("classpath:db/migration")
        .outOfOrder(true)
        .apply { if (target != null) target(target) }
        .load()
}
