package com.epam.brn.service.statistics.impl

import com.epam.brn.dto.statistics.MonthStudyStatistics
import com.epam.brn.dto.statistics.UserExercisingPeriod
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistics.UserPeriodStatisticsService
import com.epam.brn.service.statistics.progress.status.ProgressStatusManager
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class UserMonthStatisticsService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
    private val progressManager: ProgressStatusManager<List<StudyHistory>>
) : UserPeriodStatisticsService<MonthStudyStatistics> {

    override fun getStatisticsForPeriod(
        from: LocalDateTime,
        to: LocalDateTime,
        userId: Long?
    ): List<MonthStudyStatistics> {
        val tempUserId = userId ?: userAccountService.getCurrentUserDto().id
        val histories = studyHistoryRepository.getHistories(
            userId = tempUserId!!,
            from = from,
            to = to
        )
        return histories.map {
            val filteredHistories = histories.filter { historyFilter ->
                historyFilter.startTime.month == it.startTime.month
            }
            MonthStudyStatistics(
                date = it.startTime,
                exercisingTimeSeconds = filteredHistories.sumOf { studyHistory -> studyHistory.executionSeconds },
                progress = progressManager.getStatus(UserExercisingPeriod.WEEK, filteredHistories),
                exercisingDays = filteredHistories.distinctBy { studyHistory -> studyHistory.startTime.truncatedTo(ChronoUnit.DAYS) }.size
            )
        }.distinctBy {
            listOf(
                it.date.month,
                it.date.year
            )
        }
    }
}
