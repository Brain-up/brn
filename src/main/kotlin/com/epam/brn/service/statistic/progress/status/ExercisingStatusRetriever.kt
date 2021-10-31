package com.epam.brn.service.statistic.progress.status

import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus

/**
 *@author Nikolai Lazarev
 */
interface ExercisingStatusRetriever<T> {

    /**
     * Should calculate and return progress status according to the progress
     * @param progress - progress for which calculate the status
     */
    fun getStatus(progress: T): UserExercisingProgressStatus?

    /**
     * Should return list of periods for which retriever ables to calculate status
     */
    fun getSupportedPeriods(): List<UserExercisingPeriod>
}
