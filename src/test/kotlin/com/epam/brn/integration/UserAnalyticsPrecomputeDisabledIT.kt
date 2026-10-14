package com.epam.brn.integration

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.enums.BrnRole
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.repo.UserDailyAnalyticsRepository
import com.epam.brn.service.StudyHistoryService
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Regression guard for the flag off: with `brn.user.analytics.precompute.enabled=false` the whole
 * precompute path is gated out (no listener/updater/job beans), so completing an exercise behaves
 * exactly as before — the completion succeeds and nothing is written to user_daily_analytics.
 */
@WithMockUser(username = "flagoff@test.test", roles = [BrnRole.USER])
@TestPropertySource(properties = ["brn.user.analytics.precompute.enabled=false"])
class UserAnalyticsPrecomputeDisabledIT : BaseIT() {
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
    fun `completing an exercise writes no delta when the precompute path is disabled`() {
        // GIVEN a USER with one exercise
        val user = createUser(email = "flagoff@test.test", roles = mutableSetOf(createRole(BrnRole.USER)))
        val exercise = insertDefaultExercise(insertDefaultSubGroup(insertDefaultSeries(), 1), "FlagOff")

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
        val saved = studyHistoryService.save(dto)

        // THEN completion succeeds but no precompute row is produced (no listener to consume the event)
        saved.exerciseId shouldBe exercise.id
        Thread.sleep(500)
        userDailyAnalyticsRepository.findByDay(LocalDate.now()).any { it.userId == user.id } shouldBe false
    }
}
