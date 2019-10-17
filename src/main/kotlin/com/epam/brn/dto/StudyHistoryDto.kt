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
    @NotNull
    val doneTasksCount: Short,
    @NotNull
    val successTasksCount: Short,
    @NotNull
    val repetitionCount: Short
) {
    override fun toString(): String {
        return "StudyHistoryDto(userId=$userId, exerciseId=$exerciseId, startTime=$startTime, endTime=$endTime, doneTasksCount=$doneTasksCount, successTasksCount=$successTasksCount, repetitionCount=$repetitionCount)"
    }
}