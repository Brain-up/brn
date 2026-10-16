package com.epam.brn.integration

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.enums.BrnRole
import com.epam.brn.model.UserDailyAnalytics
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.repo.UserDailyAnalyticsRepository
import com.epam.brn.service.StudyHistoryService
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * End-to-end check of the incremental freshness path with the flag on (default in test props):
 * completing an exercise publishes an event that, after commit and off the request thread, folds the
 * completion into the current day's delta. Asserts the async upsert actually lands (polled, since it
 * runs on a separate thread) with the completion's spent/execution time and counts.
 */
@WithMockUser(username = "e2e@test.test", roles = [BrnRole.USER])
class UserAnalyticsIncrementalE2EIT : BaseIT() {
    @Autowired
    lateinit var studyHistoryService: StudyHistoryService

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
    fun `completing an exercise folds it into today's delta after commit`() {
        // GIVEN a USER (matching the security context) with one exercise
        val roleName = BrnRole.USER
        val user = createUser(email = "e2e@test.test", roles = mutableSetOf(createRole(roleName)))
        val exercise = insertDefaultExercise(insertDefaultSubGroup(insertDefaultSeries(), 1), "E2E")

        val startTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        val dto =
            StudyHistoryDto(
                id = null,
                exerciseId = exercise.id!!,
                startTime = startTime,
                endTime = startTime.plusSeconds(90),
                executionSeconds = 60,
                tasksCount = 10,
                replaysCount = 1,
                wrongAnswers = 0,
            )

        // WHEN the exercise is completed
        studyHistoryService.save(dto)

        // THEN after the commit the async upsert lands a single current-day delta for the completion
        val row = awaitTodayRow(user.id!!)
        row.day shouldBe LocalDate.now()
        row.doneExercises shouldBe 1
        row.uniqueDoneExercises shouldBe 1
        row.spentTime shouldBe 90L
        row.executionTime shouldBe 60L
    }

    private fun awaitTodayRow(userId: Long): UserDailyAnalytics {
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            val row = userDailyAnalyticsRepository.findByDay(LocalDate.now()).firstOrNull { it.userId == userId }
            if (row != null) return row
            Thread.sleep(50)
        }
        throw AssertionError("current-day delta was not produced within the timeout")
    }
}
