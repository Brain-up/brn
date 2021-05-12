package com.epam.brn.service.statistic.progress.status.impl.retriever

import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistic.progress.status.ExercisingStatusRetriever
import com.epam.brn.service.statistic.progress.status.UserCoolDownRetriever
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsManager
import org.springframework.stereotype.Component

/**
 *@author Nikolai Lazarev
 */
@Component
class WeekExercisingStatusRetriever(
    private val requirementsManager: StatusRequirementsManager,
    private val coolDownRetriever: UserCoolDownRetriever
) : ExercisingStatusRetriever<List<StudyHistory>> {
    override fun getWorstStatus(progress: List<StudyHistory>): UserExercisingProgressStatus? {
        val periodRequirements = requirementsManager.getPeriodRequirements(UserExercisingPeriod.WEEK)
        val maximalUserCoolDownDays = coolDownRetriever.getMaximalUserCoolDown(progress)
        return periodRequirements.first { 7 - maximalUserCoolDownDays in it.minimalRequirements until it.maximalRequirements }.status
    }

    override fun getSupportedPeriods(): List<UserExercisingPeriod> {
        return listOf(UserExercisingPeriod.WEEK)
    }
}
