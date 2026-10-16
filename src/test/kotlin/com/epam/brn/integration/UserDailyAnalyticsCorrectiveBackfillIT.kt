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
 * Exercises the corrective backfill migration (V220261013_2609.sql): it recomputes every finalized
 * day with the corrected semantics — done_exercises = ALL completions (repeats included),
 * unique_done_exercises = distinct exercises, execution_time = sum(execution_seconds). The migration
 * ran (idempotently) at startup via Flyway; here we seed study_history and run its SQL afterwards.
 */
class UserDailyAnalyticsCorrectiveBackfillIT : BaseIT() {
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
    fun `should recompute a completed day with all-completions, distinct and execution-time columns`() {
        // GIVEN a user who on one past day did one exercise twice and another once
        val roleName = "USER"
        val role = createRole(roleName)
        val user = insertDefaultUser()
        user.roleSet.add(role)
        userAccountRepository.save(user)

        val subGroup = insertDefaultSubGroup(insertDefaultSeries(), 1)
        val exerciseFirst = insertDefaultExercise(subGroup, "FirstName")
        val exerciseSecond = insertDefaultExercise(subGroup, "SecondName")

        val twoDaysAgo = LocalDate.now().minusDays(2).atStartOfDay()
        val h1 = insertDefaultStudyHistory(user, exerciseFirst, twoDaysAgo.plusHours(9L).truncatedTo(ChronoUnit.SECONDS))
        val h2 = insertDefaultStudyHistory(user, exerciseSecond, twoDaysAgo.plusHours(11L).truncatedTo(ChronoUnit.SECONDS))
        val h3 = insertDefaultStudyHistory(user, exerciseFirst, twoDaysAgo.plusHours(13L).truncatedTo(ChronoUnit.SECONDS))

        // WHEN the corrective backfill migration SQL runs
        jdbcTemplate.update(correctiveMigrationSql())

        // THEN that day's row counts all three completions, two distinct exercises, summed times
        val rows = userDailyAnalyticsRepository.findByDay(LocalDate.now().minusDays(2))
        rows.size shouldBe 1
        val row = rows.first()
        row.userId shouldBe user.id
        row.doneExercises shouldBe 3
        row.uniqueDoneExercises shouldBe 2
        row.spentTime shouldBe (h1.spentTimeInSeconds ?: 0L) + (h2.spentTimeInSeconds ?: 0L) + (h3.spentTimeInSeconds ?: 0L)
        row.executionTime shouldBe (h1.executionSeconds + h2.executionSeconds + h3.executionSeconds).toLong()
        row.firstDone shouldBe h1.startTime
        row.lastDone shouldBe h3.startTime
    }

    private fun correctiveMigrationSql(): String = ClassPathResource("db/migration/V220261013_2609.sql")
        .inputStream
        .readBytes()
        .toString(Charsets.UTF_8)
}
