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
    var tasksCount: Short?,
    var repetitionIndex: Float?,
    var listeningsCount: Int? = null,
    var rightAnswersCount: Int? = null
) {
    override fun toString(): String {
        return "StudyHistoryDto(" +
                "userId=$userId, " +
                "exerciseId=$exerciseId, " +
                "startTime=$startTime, " +
                "endTime=$endTime, " +
                "doneTasksCount=$tasksCount, " +
                "repetitionIndex=$repetitionIndex)" +
                "listeningsCount=$listeningsCount)" +
                "rightAnswersCount=$rightAnswersCount)"
    }
}
