package com.epam.brn.service.statistic.progress.status.impl

import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.service.statistic.progress.status.ProgressStatusManager
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsManager
import org.springframework.stereotype.Component

/**
 *@author Nikolai Lazarev
 */

@Component
class DailyProgressStatusManager(
    private val statusRequirementsManager: StatusRequirementsManager
) : ProgressStatusManager<Int> {

    override fun getStatus(progress: Int): UserExercisingProgressStatus {
        return statusRequirementsManager.getPeriodRequirements(UserExercisingPeriod.DAY).first {
            progress in it.minimalRequirements * 60 until it.maximalRequirements * 60
        }.status
    }
}
