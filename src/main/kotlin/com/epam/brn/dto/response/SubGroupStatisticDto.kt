package com.epam.brn.dto.response

/**
 *@author Nikolai Lazarev
 */
class SubGroupStatisticDto(
    val subGroupId: Long,
    val completedExercises: Int = 0,
    val totalExercises: Int
)
