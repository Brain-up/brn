package com.epam.brn.service.statistic.progress.status

import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus

/**
 *@author Nikolai Lazarev
 */
interface ProgressStatusManager<T> {

    /**
     * Should calculate and return a status for the user progress
     * @param progress - progress for which calculate the status
     */
    fun getStatus(periodType: UserExercisingPeriod, progress: T): UserExercisingProgressStatus?
}
