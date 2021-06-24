package com.epam.brn.service.statistic.progress.status.requirements

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod

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
