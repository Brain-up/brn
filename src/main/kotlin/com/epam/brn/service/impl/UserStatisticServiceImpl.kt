package com.epam.brn.service.impl

import com.epam.brn.dto.response.SubGroupStatisticDto
import com.epam.brn.dto.statistic.StartExerciseDto
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserStatisticService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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

    override fun getUserMonthStatistic(month: Int?, year: Int?): Map<Int, Int> {
        val localDateTime = LocalDateTime.now()
        val currentUserId = userAccountService.getUserFromTheCurrentSession().id
        val tempYear = year ?: localDateTime.year
        val tempMonth = month ?: localDateTime.monthValue
        val studyHistoriesDto = studyHistoryRepository.getMonthHistories(currentUserId!!, tempMonth, tempYear)
        return studyHistoriesDto.map {
            Pair(it.startTime.dayOfMonth, TimeUnit.SECONDS.toMinutes(it.executionSeconds.toLong()).toInt())
        }.toMap()
    }

    override fun getUserYearStatistic(year: Int?): Map<Int, Int> {
        val localDateTime = LocalDateTime.now()
        val currentUserId = userAccountService.getUserFromTheCurrentSession().id
        val tempYear = year ?: localDateTime.year
        val studyHistory = studyHistoryRepository.getYearStatistic(currentUserId!!, tempYear)
        return studyHistory.map {
            Pair(it.startTime.monthValue, TimeUnit.SECONDS.toMinutes(it.executionSeconds.toLong()).toInt())
        }.toMap()
    }

    override fun getUserDayStatistic(month: Int?, day: Int?, year: Int?): Map<String, StartExerciseDto> {
        val localDateTime = LocalDateTime.now()
        val currentUserId = userAccountService.getUserFromTheCurrentSession().id
        val tempYear = year ?: localDateTime.year
        val tempMonth = month ?: localDateTime.monthValue
        val tempDay = day ?: localDateTime.dayOfMonth
        val statistic = studyHistoryRepository.getDayStatistic(currentUserId!!, tempYear, tempMonth, tempDay)
        return statistic.map {
            Pair(
                it.startTime.toLocalTime().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME),
                StartExerciseDto(
                    id = it.exercise.id!!,
                    level = it.exercise.level,
                    repetition = it.replaysCount,
                    spentTime = it.executionSeconds,
                    tasksCount = it.tasksCount.toInt(),
                    wrongAnswers = it.wrongAnswers,
                    seriesName = it.exercise.subGroup?.series?.name,
                    subSeriesName = it.exercise.subGroup?.name
                )
            )
        }.toMap()
    }
}
