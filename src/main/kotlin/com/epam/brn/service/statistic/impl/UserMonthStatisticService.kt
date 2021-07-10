package com.epam.brn.service.statistic.impl

import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.service.statistic.progress.status.ProgressStatusManager
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserMonthStatisticService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
    private val progressManager: ProgressStatusManager<List<StudyHistory>>
) : UserPeriodStatisticService<MonthStudyStatistic> {

    override fun getStatisticForPeriod(
        from: LocalDateTime,
        to: LocalDateTime,
        userId: Long?
    ): List<MonthStudyStatistic> {
        val tempUserId = userId ?: userAccountService.getUserFromTheCurrentSession().id
        val histories = studyHistoryRepository.findAllByUserAccountIdAndStartTimeBetween(
            userId = tempUserId!!,
            from = from,
            to = to
        )
        return histories.map {
            val filteredHistories = histories.filter { historyFilter ->
                historyFilter.startTime.month == it.startTime.month
            }
            MonthStudyStatistic(
                date = it.startTime,
                exercisingTimeSeconds = filteredHistories.sumBy { studyHistory -> studyHistory.executionSeconds },
                progress = progressManager.getStatus(UserExercisingPeriod.WEEK, filteredHistories),
                exercisingDays = filteredHistories.size
            )
        }.distinctBy {
            listOf(
                it.date.month,
                it.date.year
            )
        }
    }
}
