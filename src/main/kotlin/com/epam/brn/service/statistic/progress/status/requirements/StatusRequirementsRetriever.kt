package com.epam.brn.service.statistic.progress.status.requirements

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus

/**
 *@author Nikolai Lazarev
 */
interface StatusRequirementsRetriever {

    /**
     * Should return requirements from the application.properties according to the period and status params
     * @param status - status for which look for the requirements
     * @param period - period for which look for the requirements
     * @return status requirements configured from properties from the application.properties file
     */
    fun getRequirementsForStatus(status: UserExercisingProgressStatus, period: UserExercisingPeriod): StatusRequirements
}
