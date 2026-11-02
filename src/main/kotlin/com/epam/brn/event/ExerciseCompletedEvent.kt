package com.epam.brn.event

import java.time.LocalDateTime

/** Published when an exercise is completed; consumed after commit by the analytics listener. */
data class ExerciseCompletedEvent(
    val userId: Long,
    val exerciseId: Long,
    val activityTime: LocalDateTime,
    val spentSeconds: Long,
    val executionSeconds: Long,
)
