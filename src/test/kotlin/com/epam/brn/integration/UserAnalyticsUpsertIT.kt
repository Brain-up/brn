package com.epam.brn.integration

import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.repo.UserDailyAnalyticsRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

/**
 * Covers the incremental after-commit upsert (the `ON CONFLICT` increment) that keeps the
 * precompute store fresh, independent of the event/async plumbing.
 */
class UserAnalyticsUpsertIT : BaseIT() {
    @Autowired
    lateinit var userDailyAnalyticsRepository: UserDailyAnalyticsRepository

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @AfterEach
    fun deleteAfterTest() {
        userDailyAnalyticsRepository.deleteAll()
        deleteInsertedTestData()
    }

    @Test
    fun `two daily upserts for the same user and day accumulate into one row`() {
        // GIVEN a user
        val user = insertDefaultUser()
        userAccountRepository.save(user)
        val day = LocalDate.now()
        val earlier = day.atTime(9, 0)
        val later = day.atTime(11, 0)

        // WHEN two completions on the same day are folded in: a first-today exercise (unique +1)
        // then a repeat of it (unique +0)
        userDailyAnalyticsRepository.upsertDailyDelta(user.id!!, day, 30L, 20L, 1, earlier)
        userDailyAnalyticsRepository.upsertDailyDelta(user.id!!, day, 45L, 25L, 0, later)

        // THEN exactly one row holds the summed counts and the widened first/last activity
        val rows = userDailyAnalyticsRepository.findByDay(day)
        rows.size shouldBe 1
        val row = rows.first()
        row.userId shouldBe user.id
        row.doneExercises shouldBe 2
        row.uniqueDoneExercises shouldBe 1
        row.spentTime shouldBe 75L
        row.executionTime shouldBe 45L
        row.firstDone shouldBe earlier
        row.lastDone shouldBe later
    }
}
