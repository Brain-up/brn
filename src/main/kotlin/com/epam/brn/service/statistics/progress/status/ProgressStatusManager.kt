package com.epam.brn.service.statistics.progress.status

import com.epam.brn.dto.statistics.UserExercisingPeriod
import com.epam.brn.dto.statistics.UserExercisingProgressStatus

interface ProgressStatusManager<in T> {
    /**
     * Should calculate and return a status for the user progress
     * @param progress - progress for which calculate the status
     */
    fun getStatus(
        periodType: UserExercisingPeriod,
        progress: T,
    ): UserExercisingProgressStatus?
}
