package com.epam.brn.service.statistic.progress.status.impl

import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistic.progress.status.ExercisingStatusRetriever
import com.epam.brn.service.statistic.progress.status.ProgressStatusManager
import org.springframework.stereotype.Service

/**
 *@author Nikolai Lazarev
 */

@Service
class ProgressStatusManagerImpl(
    private val retrievers: List<ExercisingStatusRetriever<Any>>
) : ProgressStatusManager<List<StudyHistory>> {

    override fun getStatus(period: UserExercisingPeriod, progress: List<StudyHistory>): UserExercisingProgressStatus? {
        val allStatuses: ArrayList<UserExercisingProgressStatus?> = ArrayList()
        retrievers.filter {
            it.getSupportedPeriods().contains(period)
        }.forEach {
            allStatuses.add(it.getWorstStatus(progress))
        }
        return allStatuses.minByOrNull { it!!.ordinal }
    }
}
