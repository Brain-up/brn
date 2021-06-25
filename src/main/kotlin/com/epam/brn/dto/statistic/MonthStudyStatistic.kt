package com.epam.brn.dto.statistic

import java.time.YearMonth

/**
 *@author Nikolai Lazarev
 */
data class MonthStudyStatistic(
    val date: YearMonth,
    val exercisingTimeSeconds: Int,
    val exercisingDays: Int,
    var progress: UserExercisingProgressStatus?
) : Statistic(progress)
