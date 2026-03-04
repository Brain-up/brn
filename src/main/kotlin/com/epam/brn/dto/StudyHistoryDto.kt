package com.epam.brn.dto

import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import java.time.LocalDateTime

data class StudyHistoryDto(
    var id: Long?,
    var exerciseId: Long,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime?,
    var executionSeconds: Int,
    var tasksCount: Short,
    var replaysCount: Int, // -- count of replays.  --
    var wrongAnswers: Int, // -- wrong answers count --
) {
    override fun toString(): String = "StudyHistoryDto(" +
        "exerciseId=$exerciseId, " +
        "startTime=$startTime, " +
        "endTime=$endTime, " +
        "executionSeconds=$executionSeconds," +
        "tasksCount=$tasksCount, " +
        "wrongAnswers=$wrongAnswers)"

    fun toEntity(
        userAccount: UserAccount,
        exercise: Exercise,
    ): StudyHistory = StudyHistory(
        userAccount = userAccount,
        exercise = exercise,
        startTime = this.startTime,
        endTime = this.endTime,
        executionSeconds = this.executionSeconds!!,
        tasksCount = this.tasksCount!!,
        wrongAnswers = this.wrongAnswers!!,
        replaysCount = this.replaysCount!!,
        repetitionIndex = replaysCount!!.toFloat() / (tasksCount!! + replaysCount!!),
        rightAnswersIndex = (tasksCount!! - wrongAnswers!!).toFloat() / tasksCount!!,
    )
}
