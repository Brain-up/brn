package com.epam.brn.dto.statistic

import java.time.LocalDateTime

/**
 *@author Nikolai Lazarev
 */

data class DayStudyStatistic(
    val date: LocalDateTime,
    val exercisingTimeSeconds: Int,
    var progress: UserExercisingProgressStatus? = null
) : Statistic(progress)
