package com.epam.brn.dto.statistic

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class MonthStudyStatistic(
    val date: LocalDateTime,
    val exercisingTimeSeconds: Int,
    val exercisingDays: Int,
    var progress: UserExercisingProgressStatus?
) : Statistic(progress) {
    fun toDto(): MonthStudyStatisticDto =
        MonthStudyStatisticDto(
            date = date.format(DateTimeFormatter.ofPattern("yyyy-MM")),
            exercisingTimeSeconds = exercisingTimeSeconds,
            exercisingDays = exercisingDays,
            progress = progress
        )
}
