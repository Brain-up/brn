package com.epam.brn.dto.statistics

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class MonthStudyStatistics(
    val date: LocalDateTime,
    val exercisingTimeSeconds: Int,
    val exercisingDays: Int,
    var progress: UserExercisingProgressStatus?,
) : Statistics(progress) {
    fun toDto(): MonthStudyStatisticDto = MonthStudyStatisticDto(
        date = date.format(DateTimeFormatter.ofPattern("yyyy-MM")),
        exercisingTimeSeconds = exercisingTimeSeconds,
        exercisingDays = exercisingDays,
        progress = progress,
    )
}
