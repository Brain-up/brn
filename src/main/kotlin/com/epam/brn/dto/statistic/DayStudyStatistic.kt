package com.epam.brn.dto.statistic

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class DayStudyStatistic(
    val date: LocalDateTime,
    val exercisingTimeSeconds: Int,
    var progress: UserExercisingProgressStatus? = null
) : Statistic(progress) {
    fun toDto(): DayStudyStatisticDto =
        DayStudyStatisticDto(
            date = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            exercisingTimeSeconds = exercisingTimeSeconds,
            progress = progress
        )
}
