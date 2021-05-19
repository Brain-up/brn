package com.epam.brn.service.statistic

import com.epam.brn.dto.statistic.Statistic

/**
 *@author Nikolai Lazarev
 */
interface UserStatisticService <T : Statistic> {

    /**
     * Should return subGroups progress for user
     * @param subGroupsIds - list of sub groups ids which statistic should be returned
     * @return list of information how many exercises subGroup has and how many exercises user has completed in the
     * subGroup
     */
    fun getSubGroupStatistic(subGroupsIds: List<Long>): List<T>
}
