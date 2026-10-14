package com.epam.brn.service.analytics

import com.epam.brn.event.ExerciseCompletedEvent
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserDailyAnalyticsRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
internal class UserAnalyticsUpdaterTest {
    @InjectMockKs
    lateinit var updater: UserAnalyticsUpdater

    @MockK
    lateinit var userDailyAnalyticsRepository: UserDailyAnalyticsRepository

    @MockK
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Test
    fun `should upsert daily delta counting a first-today completion as a unique exercise`() {
        // GIVEN
        val activityTime = LocalDateTime.of(2026, 10, 12, 21, 30, 15)
        val day = activityTime.toLocalDate()
        val event =
            ExerciseCompletedEvent(
                userId = 42L,
                exerciseId = 5L,
                activityTime = activityTime,
                spentSeconds = 73L,
                executionSeconds = 60L,
            )
        every {
            studyHistoryRepository.countExerciseCompletionsInRange(42L, 5L, day.atStartOfDay(), day.plusDays(1).atStartOfDay())
        } returns 1L
        every { userDailyAnalyticsRepository.upsertDailyDelta(42L, day, 73L, 60L, 1, activityTime) } returns 1

        // WHEN
        updater.apply(event)

        // THEN
        verify(exactly = 1) { userDailyAnalyticsRepository.upsertDailyDelta(42L, day, 73L, 60L, 1, activityTime) }
    }

    @Test
    fun `should not count a repeated-today exercise as a new unique exercise`() {
        // GIVEN
        val activityTime = LocalDateTime.of(2026, 10, 12, 21, 30, 15)
        val day = activityTime.toLocalDate()
        val event =
            ExerciseCompletedEvent(
                userId = 42L,
                exerciseId = 5L,
                activityTime = activityTime,
                spentSeconds = 73L,
                executionSeconds = 60L,
            )
        every {
            studyHistoryRepository.countExerciseCompletionsInRange(42L, 5L, day.atStartOfDay(), day.plusDays(1).atStartOfDay())
        } returns 3L
        every { userDailyAnalyticsRepository.upsertDailyDelta(42L, day, 73L, 60L, 0, activityTime) } returns 1

        // WHEN
        updater.apply(event)

        // THEN
        verify(exactly = 1) { userDailyAnalyticsRepository.upsertDailyDelta(42L, day, 73L, 60L, 0, activityTime) }
    }
}
