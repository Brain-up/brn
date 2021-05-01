package com.epam.brn.service.statistic.impl

import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.service.statistic.UserTimeGoalAchievedStrategy
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
    private val userTimeGoalAchievedStrategy: UserTimeGoalAchievedStrategy<List<*>>
) : UserPeriodStatisticService<MonthStudyStatistic> {

    override fun getStatisticForPeriod(from: LocalDate, to: LocalDate): List<MonthStudyStatistic> {
        val userFromTheCurrentSession = userAccountService.getUserFromTheCurrentSession()
        val histories =
            studyHistoryRepository.getHistories(userFromTheCurrentSession.id!!, Date.valueOf(from), Date.valueOf(to))
        return histories.map {
            val filteredHistories = histories.filter { historyFilter ->
                historyFilter.startTime.month.equals(it.startTime.month)
            }
            MonthStudyStatistic(
                month = YearMonth.of(it.startTime.year, it.startTime.month),
                exercisingTime = filteredHistories.sumBy { studyHistory -> studyHistory.executionSeconds },
                progress = userTimeGoalAchievedStrategy.doStrategy(filteredHistories)
            )
        }.distinct()
    }
}
