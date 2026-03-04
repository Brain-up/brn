package com.epam.brn.model.projection

interface ExerciseLastAttemptView {
    val exerciseId: Long
    val tasksCount: Short
    val wrongAnswers: Int
    val replaysCount: Int
}
