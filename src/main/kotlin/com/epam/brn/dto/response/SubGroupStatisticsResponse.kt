package com.epam.brn.dto.response

import com.epam.brn.dto.statistics.Statistics

/**
 *@author Nikolai Lazarev
 */
data class SubGroupStatisticsResponse(
    val subGroupId: Long,
    val completedExercises: Int = 0,
    val totalExercises: Int
) : Statistics(progress = null)
