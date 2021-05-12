package com.epam.brn.service.statistic.progress.status.requirements.impl

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsManager
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsRetriever
import org.springframework.stereotype.Service

/**
 *@author Nikolai Lazarev
 */

@Service
class StatusRequirementsManagerImpl(
    private val statusRequirementsRetriever: StatusRequirementsRetriever
) : StatusRequirementsManager {
    override fun getPeriodRequirements(period: UserExercisingPeriod): List<StatusRequirements> {
        return UserExercisingProgressStatus.values().map {
            statusRequirementsRetriever.getRequirementsForStatus(it, period)
        }
    }
}
