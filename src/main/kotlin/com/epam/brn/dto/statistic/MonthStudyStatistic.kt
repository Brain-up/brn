package com.epam.brn.dto.statistic

import java.time.LocalDateTime

data class MonthStudyStatistic(
    val date: LocalDateTime,
    val exercisingTimeSeconds: Int,
    val exercisingDays: Int,
    var progress: UserExercisingProgressStatus
) : Statistic(progress)
