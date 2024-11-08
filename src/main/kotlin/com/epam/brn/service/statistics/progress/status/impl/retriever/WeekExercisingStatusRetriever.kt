package com.epam.brn.service.statistics.progress.status.impl.retriever

import com.epam.brn.dto.statistics.UserExercisingPeriod
import com.epam.brn.dto.statistics.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistics.progress.status.ExercisingStatusRetriever
import com.epam.brn.service.statistics.progress.status.UserRestTimeRetriever
import com.epam.brn.service.statistics.progress.status.requirements.StatusRequirementsManager
import org.springframework.stereotype.Component

@Component
class WeekExercisingStatusRetriever(
    private val requirementsManager: StatusRequirementsManager,
    private val restTimeRetriever: UserRestTimeRetriever
) : ExercisingStatusRetriever<List<StudyHistory>> {
    override fun getStatus(progress: List<StudyHistory>): UserExercisingProgressStatus? {
        val periodRequirements = requirementsManager.getPeriodRequirements(UserExercisingPeriod.WEEK)
        val startTime = progress.minByOrNull { it.startTime }!!.startTime.toLocalDate()
        val endTime = progress.maxByOrNull { it.startTime }!!.startTime.toLocalDate()
        val maximalUserCoolDownDays = restTimeRetriever.getMaximalUserRestTime(
            userId = progress.first().userAccount.id,
            from = startTime,
            to = endTime
        )
        val statusRequirement =
            periodRequirements.firstOrNull { 7 - maximalUserCoolDownDays in it.minimalRequirements until it.maximalRequirements }
        return statusRequirement?.status
    }

    override fun getSupportedPeriods(): List<UserExercisingPeriod> {
        return listOf(UserExercisingPeriod.WEEK)
    }
}
