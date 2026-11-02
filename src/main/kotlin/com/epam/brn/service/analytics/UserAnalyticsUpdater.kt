package com.epam.brn.service.analytics

import com.epam.brn.event.ExerciseCompletedEvent
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserDailyAnalyticsRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Asynchronously folds a completed exercise into the user's current-day row.
 * Separate from the listener because Spring forbids `@Async` on `@TransactionalEventListener`.
 */
@Component
@ConditionalOnProperty(name = ["brn.user.analytics.precompute.enabled"], havingValue = "true")
class UserAnalyticsUpdater(
    private val userDailyAnalyticsRepository: UserDailyAnalyticsRepository,
    private val studyHistoryRepository: StudyHistoryRepository,
) {
    private val log = logger()

    @Async
    @Transactional
    fun apply(event: ExerciseCompletedEvent) {
        val day = event.activityTime.toLocalDate()
        // Already committed, so count == 1 means first completion today (a new distinct exercise).
        val completionsToday =
            studyHistoryRepository.countExerciseCompletionsInRange(
                event.userId,
                event.exerciseId,
                day.atStartOfDay(),
                day.plusDays(1).atStartOfDay(),
            )
        val uniqueIncrement = if (completionsToday <= 1L) 1 else 0
        userDailyAnalyticsRepository.upsertDailyDelta(
            event.userId,
            day,
            event.spentSeconds,
            event.executionSeconds,
            uniqueIncrement,
            event.activityTime,
        )
        log.debug("Applied precompute analytics increment for user ${event.userId} on day $day")
    }
}
