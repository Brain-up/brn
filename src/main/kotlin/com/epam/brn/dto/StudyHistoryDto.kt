package com.epam.brn.dto

import java.time.LocalDateTime
import javax.validation.constraints.NotNull
import org.springframework.http.HttpStatus

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
    var responseCode: HttpStatus? = null
) {
    override fun toString(): String {
        return "StudyHistoryDto(userId=$userId, exerciseId=$exerciseId, startTime=$startTime, endTime=$endTime, doneTasksCount=$tasksCount, repetitionIndex=$repetitionIndex, responseCode=$responseCode)"
    }
}
