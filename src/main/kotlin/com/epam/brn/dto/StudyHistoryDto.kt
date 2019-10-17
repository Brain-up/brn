package com.epam.brn.dto

import java.time.LocalDateTime


class StudyHistoryDto(
    val userId: Long,
    val exerciseId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val doneTasksCount: Short,
    val successTasksCount: Short,
    val repetitionCount: Short
) {
    override fun toString(): String {
        return "StudyHistoryDto(userId=$userId, exerciseId=$exerciseId, startTime=$startTime, endTime=$endTime, doneTasksCount=$doneTasksCount, successTasksCount=$successTasksCount, repetitionCount=$repetitionCount)"
    }
}