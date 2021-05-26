package com.epam.brn.dto.statistic

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class MonthStudyStatistic(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val date: LocalDateTime,
    val exercisingTimeSeconds: Int,
    val exercisingDays: Int,
    var progress: UserExercisingProgressStatus
) : Statistic(progress)
