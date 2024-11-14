package com.epam.brn.service.statistics

import com.epam.brn.dto.statistics.Statistics

/**
 *@author Nikolai Lazarev
 */
interface UserStatisticService <T : Statistics> {

    /**
     * Should return subGroups progress for user
     * @param subGroupsIds - list of sub groups ids which statistics should be returned
     * @return list of information how many exercises subGroup has and how many exercises user has completed in the
     * subGroup
     */
    fun getSubGroupStatistic(subGroupsIds: List<Long>): List<T>
}
