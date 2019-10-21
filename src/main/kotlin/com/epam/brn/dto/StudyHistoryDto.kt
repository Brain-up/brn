package com.epam.brn.dto

import java.time.LocalDateTime
import javax.validation.constraints.NotNull

class StudyHistoryDto(
    @NotNull
    val userId: Long,
    @NotNull
    val exerciseId: Long,
    @NotNull
    val startTime: LocalDateTime,
    @NotNull
    val endTime: LocalDateTime,
    val doneTasksCount: Short = 0,
    val successTasksCount: Short = 0,
    val repetitionCount: Short = 0
) {
    override fun toString(): String {
        return "StudyHistoryDto(userId=$userId, exerciseId=$exerciseId, startTime=$startTime, endTime=$endTime, doneTasksCount=$doneTasksCount, successTasksCount=$successTasksCount, repetitionCount=$repetitionCount)"
    }
}