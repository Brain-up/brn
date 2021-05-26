package com.epam.brn.dto.statistic

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

/**
 *@author Nikolai Lazarev
 */

data class DayStudyStatistic(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val date: LocalDateTime,
    val exercisingTimeSeconds: Int,
    var progress: UserExercisingProgressStatus? = null
) : Statistic(progress)
