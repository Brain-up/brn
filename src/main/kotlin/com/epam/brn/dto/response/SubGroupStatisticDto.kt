package com.epam.brn.dto.response

import com.epam.brn.dto.statistic.Statistic

/**
 *@author Nikolai Lazarev
 */
data class SubGroupStatisticDto(
    val subGroupId: Long,
    val completedExercises: Int = 0,
    val totalExercises: Int
) : Statistic(progress = null)
