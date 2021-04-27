package com.epam.brn.dto.statistic

import java.time.LocalDate

/**
 *@author Nikolai Lazarev
 */

data class StudyStatistic(
    val date: LocalDate,
    val exercisingTime: Int
) : Statistic()
