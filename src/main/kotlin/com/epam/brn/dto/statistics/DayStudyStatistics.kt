package com.epam.brn.dto.statistics

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class DayStudyStatistics(
    val date: LocalDateTime,
    val exercisingTimeSeconds: Int,
    var progress: UserExercisingProgressStatus? = null,
) : Statistics(progress) {
    fun toDto(): DayStudyStatisticDto =
        DayStudyStatisticDto(
            date = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            exercisingTimeSeconds = exercisingTimeSeconds,
            progress = progress,
        )
}
