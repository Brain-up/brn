package com.epam.brn.dto.statistic

/**
 *@author Nikolai Lazarev
 */
data class StatusRequirements(
    var status: UserExercisingProgressStatus,
    var minimalRequirements: Int,
    var maximalRequirements: Int
)
