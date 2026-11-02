package com.epam.brn.integration

import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.repo.UserDailyAnalyticsRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Re-runs the backfill migration (V220261013_2609.sql) after seeding study_history and checks
 * per-day grouping and that today is excluded.
 */
class UserDailyAnalyticsBackfillIT : BaseIT() {
    @Autowired
    lateinit var userDailyAnalyticsRepository: UserDailyAnalyticsRepository

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun deleteAfterTest() {
        userDailyAnalyticsRepository.deleteAll()
        deleteInsertedTestData()
    }

    @Test
    fun `should backfill one delta per user and completed day, excluding today`() {
        // GIVEN a user with history on two past days and today
        val user = insertDefaultUser()
        userAccountRepository.save(user)

        val subGroup = insertDefaultSubGroup(insertDefaultSeries(), 1)
        val exerciseFirst = insertDefaultExercise(subGroup, "FirstName")
        val exerciseSecond = insertDefaultExercise(subGroup, "SecondName")

        val threeDaysAgo = LocalDate.now().minusDays(3).atStartOfDay()
        val twoDaysAgo = LocalDate.now().minusDays(2).atStartOfDay()
        val today = LocalDate.now().atStartOfDay()

        insertDefaultStudyHistory(user, exerciseFirst, threeDaysAgo.plusHours(9L).truncatedTo(ChronoUnit.SECONDS))
        insertDefaultStudyHistory(user, exerciseFirst, twoDaysAgo.plusHours(9L).truncatedTo(ChronoUnit.SECONDS))
        insertDefaultStudyHistory(user, exerciseSecond, twoDaysAgo.plusHours(11L).truncatedTo(ChronoUnit.SECONDS))
        insertDefaultStudyHistory(user, exerciseFirst, today.plusHours(9L).truncatedTo(ChronoUnit.SECONDS))

        // WHEN the backfill migration SQL runs
        jdbcTemplate.update(backfillMigrationSql())

        // THEN one row per completed day; today is excluded
        val rows = userDailyAnalyticsRepository.findByUserIdOrderByDay(user.id!!)
        rows.map { it.day } shouldBe listOf(LocalDate.now().minusDays(3), LocalDate.now().minusDays(2))
        rows[0].doneExercises shouldBe 1
        rows[1].doneExercises shouldBe 2
    }

    private fun backfillMigrationSql(): String = ClassPathResource("db/migration/V220261013_2609.sql")
        .inputStream
        .readBytes()
        .toString(Charsets.UTF_8)
}
