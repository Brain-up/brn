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
 * Applies a single completed exercise to the precompute store: folds it into that user's current-day
 * delta via an atomic upsert. Runs `@Async` (off the request thread) so exercise completion is never
 * delayed; invoked by [UserAnalyticsUpdateListener] after the study-history commit.
 *
 * All-time ("lifetime") figures are intentionally not stored here — they derive from the daily deltas
 * (`SUM`/`MIN`/`MAX` over a user's rows) the same way period roll-ups do, materialized only if a reader
 * ever measures a need.
 *
 * Kept separate from the event listener on purpose: `@Async` must not sit on a
 * `@TransactionalEventListener` method (Spring's `RestrictedTransactionalEventListenerFactory`
 * forbids it), so the listener delegates here.
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
        // This completion is already committed, so a count of 1 means it is the first (and only) time
        // the user did this exercise today -> it adds a distinct exercise; >1 means it is a repeat.
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
