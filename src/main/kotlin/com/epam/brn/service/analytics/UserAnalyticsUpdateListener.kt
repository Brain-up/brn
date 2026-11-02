package com.epam.brn.service.analytics

import com.epam.brn.event.ExerciseCompletedEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * After commit, passes [ExerciseCompletedEvent] to the async [UserAnalyticsUpdater].
 * Dropped increments are healed by the nightly reconcile.
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
