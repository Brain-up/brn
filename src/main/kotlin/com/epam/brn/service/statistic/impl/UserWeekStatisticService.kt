package com.epam.brn.service.statistic.impl

import com.epam.brn.dto.statistic.WeekStudyStatistic
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.service.statistic.UserTimeGoalAchievedStrategy
import java.sql.Date
import java.time.LocalDate

/**
 *@author Nikolai Lazarev
 */
class UserWeekStatisticService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
    private val userTimeGoalAchievedStrategy: UserTimeGoalAchievedStrategy<Int>
) : UserPeriodStatisticService<WeekStudyStatistic> {
    override fun getStatisticForPeriod(from: LocalDate, to: LocalDate): List<WeekStudyStatistic> {
        val user = userAccountService.getUserFromTheCurrentSession()
        val studyHistories = studyHistoryRepository.getHistories(
            user.id!!,
            Date.valueOf(from),
            Date.valueOf(to)
        )
        val statistic = studyHistories.map {
            WeekStudyStatistic(
                exercisingTime = studyHistories.filter { studyHistory ->
                    studyHistory.startTime.monthValue == it.startTime.monthValue &&
                        studyHistory.startTime.dayOfMonth == it.startTime.dayOfMonth &&
                        studyHistory.startTime.year == it.startTime.year
                }.map {
                    it.executionSeconds
                }.sum(),
                date = it.startTime.toLocalDate()
            )
        }.distinct()
        statistic.forEach {
            it.progress = userTimeGoalAchievedStrategy.doStrategy(it.exercisingTime)
        }
        return statistic
    }
}
