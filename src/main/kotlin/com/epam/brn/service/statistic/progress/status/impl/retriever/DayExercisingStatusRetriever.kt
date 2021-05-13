package com.epam.brn.service.statistic.progress.status.impl.retriever

import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistic.progress.status.ExercisingStatusRetriever
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsManager
import org.springframework.stereotype.Component

/**
 *@author Nikolai Lazarev
 */

@Component
class DayExercisingStatusRetriever(
    private val requirementsManager: StatusRequirementsManager
) : ExercisingStatusRetriever<List<StudyHistory>> {
    override fun getWorstStatus(progress: List<StudyHistory>): UserExercisingProgressStatus? {
        val periodRequirements = requirementsManager.getPeriodRequirements(UserExercisingPeriod.DAY)
        return progress.map {
            periodRequirements.first { requirements ->
                it.executionSeconds in requirements.minimalRequirements * 60 until requirements.maximalRequirements * 60
            }.status
        }.minByOrNull { status -> status.ordinal }
    }

    override fun getSupportedPeriods(): List<UserExercisingPeriod> {
        return listOf(UserExercisingPeriod.WEEK, UserExercisingPeriod.DAY)
    }
}
