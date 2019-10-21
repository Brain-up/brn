package com.epam.brn.dto

import java.time.LocalDateTime

class StudyHistoryDto(
    var userId: Long?,
    var exerciseId: Long?,
    var startTime: LocalDateTime?,
    var endTime: LocalDateTime?,
    var doneTasksCount: Short?,
    var successTasksCount: Short?,
    var repetitionCount: Short?
) {
    constructor() : this(null, null, null, null, null, null, null)

    override fun toString(): String {
        return "StudyHistoryDto(userId=$userId, exerciseId=$exerciseId, startTime=$startTime, endTime=$endTime, doneTasksCount=$doneTasksCount, successTasksCount=$successTasksCount, repetitionCount=$repetitionCount)"
    }
}