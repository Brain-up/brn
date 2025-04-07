package com.epam.brn.service.statistics.progress.status.requirements.impl

import com.epam.brn.dto.statistics.StatusRequirements
import com.epam.brn.dto.statistics.UserExercisingPeriod
import com.epam.brn.dto.statistics.UserExercisingProgressStatus
import com.epam.brn.service.statistics.progress.status.requirements.StatusRequirementsRetriever
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import javax.naming.OperationNotSupportedException

/**
 *@author Nikolai Lazarev
 */

@Component
class ApplicationPropertiesRequirementsRetriever(
    private val environment: Environment,
) : StatusRequirementsRetriever {
    override fun getRequirementsForStatus(
        status: UserExercisingProgressStatus,
        period: UserExercisingPeriod,
    ): StatusRequirements {
        val periodName = period.name.lowercase()
        val statusName = status.name.lowercase()
        return StatusRequirements(
            maximalRequirements =
                environment
                    .getProperty("brn.statistics.progress.$periodName.status.$statusName.maximal")
                    ?.toInt()
                    ?: throw OperationNotSupportedException(
                        "Maximal requirements for period: $periodName or status: $statusName are not supported yet",
                    ),
            minimalRequirements =
                environment
                    .getProperty("brn.statistics.progress.$periodName.status.$statusName.minimal")
                    ?.toInt()
                    ?: throw OperationNotSupportedException(
                        "Minimal requirements for period: $periodName or status: $statusName are not supported yet",
                    ),
            status = status,
        )
    }
}
