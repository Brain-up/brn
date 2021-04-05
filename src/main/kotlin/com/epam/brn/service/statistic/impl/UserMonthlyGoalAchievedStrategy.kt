package com.epam.brn.service.statistic.impl

import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserTimeGoalAchievedStrategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.YearMonth

/**
 *@author Nikolai Lazarev
 */

@Service
class UserMonthlyGoalAchievedStrategy(
    val userAccountService: UserAccountService,
    val studyHistoryRepository: StudyHistoryRepository,
    @Value("\${brn.maxCoolDownDays}")
    val maxCoolDownDays: Int
) : UserTimeGoalAchievedStrategy<YearMonth> {
    override fun isGoalAchieved(time: YearMonth): Boolean {
        val user = userAccountService.getUserFromTheCurrentSession()
        val studyHistories = studyHistoryRepository.getMonthHistories(user.id!!, time.monthValue, time.year)
        return studyHistories.distinct().size >= time.month.length(time.isLeapYear) - maxCoolDownDays
    }
}
