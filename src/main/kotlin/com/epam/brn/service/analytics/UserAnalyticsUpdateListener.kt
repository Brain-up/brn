package com.epam.brn.service.analytics

import com.epam.brn.event.ExerciseCompletedEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Keeps the precomputed-analytics store fresh on the completion path: on [ExerciseCompletedEvent] it
 * hands the completion to [UserAnalyticsUpdater] to be applied asynchronously.
 *
 * Runs only when `brn.user.analytics.precompute.enabled=true`. Fires AFTER_COMMIT so only durably
 * committed history is counted. The actual upserts run `@Async` in [UserAnalyticsUpdater] so exercise
 * completion is never delayed; a dropped increment (restart, executor rejection) is tolerated — the
 * nightly reconcile recomputes each affected day from `study_history` and overwrites any drift.
 */
@Component
@ConditionalOnProperty(name = ["brn.user.analytics.precompute.enabled"], havingValue = "true")
class UserAnalyticsUpdateListener(
    private val userAnalyticsUpdater: UserAnalyticsUpdater,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onExerciseCompleted(event: ExerciseCompletedEvent) {
        userAnalyticsUpdater.apply(event)
    }
}
