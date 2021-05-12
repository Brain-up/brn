package com.epam.brn.service.statistic.progress.status.requirements.impl

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsRetriever
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import javax.naming.OperationNotSupportedException

/**
 *@author Nikolai Lazarev
 */

@Component
class ApplicationPropertiesRequirementsRetriever(
    private val environment: Environment
) : StatusRequirementsRetriever {
    override fun getRequirementsForStatus(
        status: UserExercisingProgressStatus,
        period: UserExercisingPeriod
    ): StatusRequirements {
        return StatusRequirements(
            maximalRequirements = environment.getProperty("brn.statistic.progress.${period.name}.status.${status.name}.maximal")
                ?.toInt()
                ?: throw OperationNotSupportedException("Maximal requirements for period: ${period.name} or status: ${period.name} are not supported yet"),
            minimalRequirements = environment.getProperty("brn.statistic.progress.${period.name}.status.${status.name}.minimal")
                ?.toInt()
                ?: throw OperationNotSupportedException("Minimal requirements for period: ${period.name} or status: ${period.name} are not supported yet"),
            status = status
        )
    }
}
