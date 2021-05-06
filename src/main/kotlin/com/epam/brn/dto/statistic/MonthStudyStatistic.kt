package com.epam.brn.dto.statistic

import java.time.YearMonth

/**
 *@author Nikolai Lazarev
 */
data class MonthStudyStatistic(
    val month: YearMonth,
    val exercisingTime: Int,
    var progress: UserExercisingProgressStatus
) : Statistic(progress)
