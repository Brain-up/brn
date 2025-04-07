package com.epam.brn.service.statistics.progress.status.impl

import com.epam.brn.dto.statistics.UserExercisingPeriod
import com.epam.brn.dto.statistics.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistics.progress.status.ExercisingStatusRetriever
import com.epam.brn.service.statistics.progress.status.ProgressStatusManager
import org.springframework.stereotype.Service

@Service
class StudyHistoriesProgressStatusManager(
    private val retrievers: List<ExercisingStatusRetriever<List<*>>>,
) : ProgressStatusManager<List<StudyHistory>> {
    override fun getStatus(
        periodType: UserExercisingPeriod,
        progress: List<StudyHistory>,
    ): UserExercisingProgressStatus? =
        retrievers
            .filter { it.getSupportedPeriods().contains(periodType) }
            .mapNotNull { it.getStatus(progress) }
            .minByOrNull { it.ordinal }
}
