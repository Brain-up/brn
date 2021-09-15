package com.epam.brn.dto

import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import java.time.LocalDateTime
import javax.validation.constraints.NotNull

data class StudyHistoryDto(
    var id: Long?,
    @field:NotNull
    var exerciseId: Long?,
    @field:NotNull
    var startTime: LocalDateTime,
    var endTime: LocalDateTime?,
    @field:NotNull
    var executionSeconds: Int?,
    @field:NotNull
    var tasksCount: Short?,
    @field:NotNull
    var replaysCount: Int?, // -- count of replays.  --
    @field:NotNull
    var wrongAnswers: Int? // -- wrong answers count --
) {
    override fun toString(): String = "StudyHistoryDto(" +
        "exerciseId=$exerciseId, " +
        "startTime=$startTime, " +
        "endTime=$endTime, " +
        "executionSeconds=$executionSeconds," +
        "tasksCount=$tasksCount, " +
        "wrongAnswers=$wrongAnswers)"

    fun toEntity(userAccount: UserAccount, exercise: Exercise): StudyHistory = StudyHistory(
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
