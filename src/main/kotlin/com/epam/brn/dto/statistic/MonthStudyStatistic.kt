package com.epam.brn.dto.statistic

import java.time.YearMonth

/**
 *@author Nikolai Lazarev
 */
data class MonthStudyStatistic(
    val date: YearMonth,
    val exercisingTime: Int,
    var progress: UserExercisingProgressStatus
) : Statistic(progress)
