package com.epam.brn.service.impl

import com.epam.brn.dto.response.SubGroupStatisticDto
import com.epam.brn.dto.statistic.StartExerciseDto
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserStatisticService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.Year
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 *@author Nikolai Lazarev
 */

@Service
class UserStatisticServiceImpl(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userAccountService: UserAccountService
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

    override fun getUserMonthStatistic(month: Int, year: Int?): Map<Int, Int> {
        val currentUserId = userAccountService.getUserFromTheCurrentSession().id
        val tempYear = year ?: Calendar.getInstance()[Calendar.YEAR]
        val studyHistoriesDto = studyHistoryRepository.getMonthHistories(currentUserId!!, month, tempYear)
        return studyHistoriesDto.map {
            Pair(it.startTime.dayOfMonth, TimeUnit.SECONDS.toMinutes(it.executionSeconds.toLong()).toInt())
        }.toMap()
    }

    override fun getUserYearStatistic(year: Year): Map<Int, Int> {
        TODO("Not yet implemented")
    }

    override fun getUserDayStatistic(month: Int, day: Int, year: Year): Map<LocalDateTime, StartExerciseDto> {
        TODO("Not yet implemented")
    }
}
