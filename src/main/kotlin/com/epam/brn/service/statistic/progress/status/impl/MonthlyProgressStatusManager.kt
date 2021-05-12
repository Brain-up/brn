package com.epam.brn.service.statistic.progress.status.impl

import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistic.progress.status.ProgressStatusManager
import com.epam.brn.service.statistic.progress.status.UserCoolDownRetriever
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsManager
import org.springframework.stereotype.Component

/**
 *@author Nikolai Lazarev
 */
@Component
class MonthlyProgressStatusManager(
    private val statusRequirementsManager: StatusRequirementsManager,
    private val coolDownRetriever: UserCoolDownRetriever
) : ProgressStatusManager<List<StudyHistory>> {
    override fun getStatus(progress: List<StudyHistory>): UserExercisingProgressStatus {
        val weekPeriodRequirements = statusRequirementsManager.getPeriodRequirements(UserExercisingPeriod.WEEK)
        val dayPeriodRequirements = statusRequirementsManager.getPeriodRequirements(UserExercisingPeriod.DAY)
        val maximalUserCoolDownDays = coolDownRetriever.getMaximalUserCoolDown(progress)

        val userWorstMonthStatus =
            weekPeriodRequirements.first { 7 - maximalUserCoolDownDays in it.minimalRequirements until it.maximalRequirements }.status
        val userWorstDayStatus = progress.map {
            dayPeriodRequirements.first { requirements ->
                it.executionSeconds in requirements.minimalRequirements * 60 until requirements.maximalRequirements * 60
            }.status
        }.minByOrNull { status -> status.ordinal }

        return if (userWorstMonthStatus.ordinal > userWorstDayStatus!!.ordinal) {
            userWorstDayStatus
        } else
            userWorstMonthStatus
    }
}
