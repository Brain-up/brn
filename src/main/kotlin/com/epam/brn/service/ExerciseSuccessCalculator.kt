package com.epam.brn.service

import com.epam.brn.model.StudyHistory
import com.epam.brn.model.projection.ExerciseLastAttemptView
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Single source of truth for deciding whether an exercise attempt was done well ("successful").
 * The thresholds come from the `minRepetitionIndex` / `minRightAnswersIndex` properties.
 */
@Service
class ExerciseSuccessCalculator {
    @Value(value = "\${minRepetitionIndex}")
    private lateinit var minRepetitionIndex: Number

    @Value(value = "\${minRightAnswersIndex}")
    private lateinit var minRightAnswersIndex: Number

    fun isSuccessful(
        tasksCount: Short,
        wrongAnswers: Int,
        replaysCount: Int,
    ): Boolean {
        val repetitionIndex = tasksCount.toFloat() / (replaysCount + tasksCount)
        val rightAnswersIndex = 1F - wrongAnswers.toFloat() / tasksCount
        return repetitionIndex >= minRepetitionIndex.toFloat() && rightAnswersIndex >= minRightAnswersIndex.toFloat()
    }

    fun isSuccessful(studyHistory: StudyHistory): Boolean =
        isSuccessful(studyHistory.tasksCount, studyHistory.wrongAnswers, studyHistory.replaysCount)

    fun isSuccessful(lastAttempt: ExerciseLastAttemptView): Boolean =
        isSuccessful(lastAttempt.tasksCount, lastAttempt.wrongAnswers, lastAttempt.replaysCount)
}
