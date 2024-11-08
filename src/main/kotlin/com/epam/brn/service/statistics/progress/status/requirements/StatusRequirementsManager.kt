package com.epam.brn.service.statistics.progress.status.requirements

import com.epam.brn.dto.statistics.StatusRequirements
import com.epam.brn.dto.statistics.UserExercisingPeriod

/**
 *@author Nikolai Lazarev
 */
interface StatusRequirementsManager {

    /**
     * Should return list of requirements generated according to the period
     * @param period - period for which look for the requirements
     * @return list of the status requirements
     */
    fun getPeriodRequirements(period: UserExercisingPeriod): List<StatusRequirements>
}
