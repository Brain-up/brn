package com.epam.brn.service.statistic.impl

import com.epam.brn.dto.response.SubGroupStatisticDto
import com.epam.brn.dto.statistic.StudyStatistic
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserStatisticService
import com.epam.brn.service.statistic.UserTimeGoalAchievedStrategy
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.LocalDate
import java.time.YearMonth

/**
 *@author Nikolai Lazarev
 */

@Service
class UserStatisticServiceImpl(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userAccountService: UserAccountService,
    private val monthlyGoalAchievedStrategy: UserTimeGoalAchievedStrategy<YearMonth>
) : UserStatisticService {

    override fun getSubGroupStatistic(subGroupsIds: List<Long>): List<SubGroupStatisticDto> {
        val userAccount = userAccountService.getUserFromTheCurrentSession()
        return subGroupsIds.map {
            SubGroupStatisticDto(
                subGroupId = it,
                totalExercises = exerciseRepository.findExercisesBySubGroupId(it).size,
                completedExercises = studyHistoryRepository.getDoneExercises(it, userAccount.id!!).size
            )
        }.toList()
    }

    override fun getUserStatisticForPeriod(from: LocalDate, to: LocalDate): List<StudyStatistic> {
        val user = userAccountService.getUserFromTheCurrentSession()
        val studyHistories = studyHistoryRepository.getHistories(user.id!!, Date.valueOf(from), Date.valueOf(to))
        return studyHistories.map {
            StudyStatistic(
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
    }
}
