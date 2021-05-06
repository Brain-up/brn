package com.epam.brn.dto.statistic

import java.time.LocalDate

/**
 *@author Nikolai Lazarev
 */

data class DayStudyStatistic(
    val date: LocalDate,
    val exercisingTime: Int,
    var progress: UserExercisingProgressStatus? = null
) : Statistic(progress)
