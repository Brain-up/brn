package com.epam.brn.event

import java.time.LocalDateTime

/**
 * Published after a [com.epam.brn.model.StudyHistory] row is persisted (an exercise is completed).
 * Carries just what the precomputed-analytics store needs to fold the completion into the user's
 * current-day delta. Consumed off the request thread, after commit, by
 * [com.epam.brn.service.analytics.UserAnalyticsUpdateListener].
 */
data class ExerciseCompletedEvent(
    val userId: Long,
    val exerciseId: Long,
    val activityTime: LocalDateTime,
    val spentSeconds: Long,
    val executionSeconds: Long,
)
