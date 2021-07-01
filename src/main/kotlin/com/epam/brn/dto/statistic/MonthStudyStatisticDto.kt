package com.epam.brn.dto.statistic

/**
 * This class created to support legacy date format in responses and
 * should be removed once support of the controllers with legacy date format will
 * be stopped
 */

data class MonthStudyStatisticDto(
    val date: String,
    val exercisingTimeSeconds: Int,
    val exercisingDays: Int,
    var progress: UserExercisingProgressStatus?
)
