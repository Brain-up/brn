package com.epam.brn.service.statistic.impl

import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.LocalDateTime

@Service
class UserMonthStatisticService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
) : UserPeriodStatisticService<MonthStudyStatistic> {

    override fun getStatisticForPeriod(
        from: LocalDateTime,
        to: LocalDateTime,
        userId: Long?
    ): List<MonthStudyStatistic> {
        val tempUserId = userId ?: userAccountService.getUserFromTheCurrentSession().id
        val histories =
            studyHistoryRepository.getHistories(
                tempUserId!!,
                Date.valueOf(from.toLocalDate()),
                Date.valueOf(to.toLocalDate())
            )
        return histories.map {
            val filteredHistories = histories.filter { historyFilter ->
                historyFilter.startTime.month == it.startTime.month
            }
            MonthStudyStatistic(
                date = it.startTime,
                exercisingTimeSeconds = filteredHistories.sumBy { studyHistory -> studyHistory.executionSeconds },
                progress = UserExercisingProgressStatus.GREAT,
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
