package com.epam.brn.dto

import java.time.LocalDateTime
import javax.validation.constraints.NotNull

data class StudyHistoryDto(
    var id: Long? = null,
    @NotNull
    var userId: Long?,
    @NotNull
    var exerciseId: Long?,
    var startTime: LocalDateTime?,
    var endTime: LocalDateTime?,
    var executionSeconds: Int? = null,
    var tasksCount: Short?,
    var listeningsCount: Int? = null, // -- count of all user listenings. >=tasksCount --
    var repetitionIndex: Float?, // repetitionIndex=tasksCount/listeningsCount
    var rightAnswersCount: Int? = null, // -- right answers from 1 time <=tasksCount --
    var rightAnswersIndex: Float? // rightAnswersIndex=rightAnswersCount/tasksCount
) {
    override fun toString(): String {
        return "StudyHistoryDto(" +
                "userId=$userId, " +
                "exerciseId=$exerciseId, " +
                "startTime=$startTime, " +
                "endTime=$endTime, " +
                "executionSeconds=$executionSeconds," +
                "doneTasksCount=$tasksCount, " +
                "repetitionIndex=$repetitionIndex," +
                "listeningsCount=$listeningsCount," +
                "rightAnswersCount=$rightAnswersCount," +
                "rightAnswersIndex=$rightAnswersIndex)"
    }
}
