package com.epam.brn.dto.statistic

import java.time.LocalDate

/**
 *@author Nikolai Lazarev
 */

data class WeekStudyStatistic(
    val date: LocalDate,
    val exercisingTime: Int,
    var progress: Int = 0
) : Statistic()
