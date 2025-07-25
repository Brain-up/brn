package com.epam.brn.service.statistics.impl

import com.epam.brn.dto.statistics.DayStudyStatistics
import com.epam.brn.dto.statistics.UserExercisingPeriod
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistics.UserPeriodStatisticsService
import com.epam.brn.service.statistics.progress.status.ProgressStatusManager
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserDayStatisticsService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
    private val progressManager: ProgressStatusManager<List<StudyHistory>>,
) : UserPeriodStatisticsService<DayStudyStatistics> {
    override fun getStatisticsForPeriod(
        from: LocalDateTime,
        to: LocalDateTime,
        userId: Long?,
    ): List<DayStudyStatistics> {
        val tempUserId = userId ?: userAccountService.getCurrentUserDto().id
        val studyHistories =
            studyHistoryRepository.getHistories(
                userId = tempUserId!!,
                from = from,
                to = to,
            )
        return studyHistories
            .map {
                val filteredStudyHistories =
                    studyHistories.filter { studyHistoryFilter ->
                        studyHistoryFilter.startTime.toLocalDate() == it.startTime.toLocalDate()
                    }
                DayStudyStatistics(
                    exercisingTimeSeconds = filteredStudyHistories.sumOf { dayStudyHistory -> dayStudyHistory.executionSeconds },
                    date = it.startTime,
                    progress = progressManager.getStatus(UserExercisingPeriod.DAY, filteredStudyHistories),
                )
            }.distinctBy { it.date.toLocalDate() }
    }
}
