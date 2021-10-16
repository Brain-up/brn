package com.epam.brn.service.statistic.progress.status.impl.retriever

import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistic.progress.status.ExercisingStatusRetriever
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsManager
import org.springframework.stereotype.Component

@Component
class DayExercisingStatusRetriever(
    private val requirementsManager: StatusRequirementsManager
) : ExercisingStatusRetriever<List<StudyHistory>> {
    override fun getStatus(progress: List<StudyHistory>): UserExercisingProgressStatus? {
        val periodRequirements = requirementsManager.getPeriodRequirements(UserExercisingPeriod.DAY)
        val sumOfHistory = progress.sumBy { it.executionSeconds }
        return periodRequirements.firstOrNull { requirements ->
            sumOfHistory in requirements.minimalRequirements * 60 until requirements.maximalRequirements * 60
        }?.status
    }

    override fun getSupportedPeriods(): List<UserExercisingPeriod> {
        return listOf(UserExercisingPeriod.WEEK, UserExercisingPeriod.DAY)
    }
}
