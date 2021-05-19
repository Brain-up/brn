package com.epam.brn.service.statistic.impl

import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.service.statistic.progress.status.ProgressStatusManager
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.LocalDate
import java.time.YearMonth

/**
 *@author Nikolai Lazarev
 */

@Service
class UserMonthStatisticService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
    private val progressManager: ProgressStatusManager<List<StudyHistory>>
) : UserPeriodStatisticService<MonthStudyStatistic> {

    override fun getStatisticForPeriod(from: LocalDate, to: LocalDate, userId: Long?): List<MonthStudyStatistic> {
        val tempUserId = userId ?: userAccountService.getUserFromTheCurrentSession().id
        val histories =
            studyHistoryRepository.getHistories(tempUserId!!, Date.valueOf(from), Date.valueOf(to))
        return histories.map {
            val filteredHistories = histories.filter { historyFilter ->
                historyFilter.startTime.month == it.startTime.month
            }
            MonthStudyStatistic(
                date = YearMonth.of(it.startTime.year, it.startTime.month),
                exercisingTimeSeconds = filteredHistories.sumBy { studyHistory -> studyHistory.executionSeconds },
                progress = progressManager.getStatus(UserExercisingPeriod.WEEK, filteredHistories),
                exercisingDays = filteredHistories.size
            )
        }.distinct()
    }
}
