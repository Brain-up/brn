package com.epam.brn.service

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.statistic.UserDailyDetailStatisticsDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class StudyHistoryService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userAccountService: UserAccountService
) {

    fun getTodayTimer(): Int {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return studyHistoryRepository.getTodayDayTimer(currentUser.id!!)
    }

    fun save(studyHistoryDto: StudyHistoryDto): StudyHistoryDto {
        val currentUser = userAccountService.getCurrentUser()
        val exercise = exerciseRepository
            .findById(studyHistoryDto.exerciseId!!)
            .orElseThrow { EntityNotFoundException("Exercise with exerciseId '${studyHistoryDto.exerciseId}' doesn't exist.") }
        val newStudyHistory = studyHistoryDto.toEntity(currentUser, exercise)
        val savedStudyHistory = studyHistoryRepository.save(newStudyHistory)

        return savedStudyHistory.toDto()
    }

    fun calculateDiffInSeconds(start: LocalDateTime, end: LocalDateTime): Int {
        return ChronoUnit.SECONDS.between(start, end).toInt()
    }

    @Deprecated(message = "This is a legacy method. Use the same method with LocalDateTime as args for the dates instead")
    fun getHistoriesForCurrentUser(from: LocalDate, to: LocalDate): List<StudyHistoryDto> {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return getHistories(currentUser.id!!, from, to)
    }

    @Deprecated(message = "This is a legacy method. Use the same method with LocalDateTime as args for the dates instead")
    fun getHistories(userId: Long, from: LocalDate, to: LocalDate): List<StudyHistoryDto> {
        return studyHistoryRepository.getHistories(
            userId,
            Date.valueOf(from),
            Date.valueOf(to)
        )
            .map { it.toDto() }
    }

    fun getHistoriesForCurrentUser(from: LocalDateTime, to: LocalDateTime): List<StudyHistoryDto> {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return getHistories(currentUser.id!!, from, to)
    }

    fun getHistories(userId: Long, from: LocalDateTime, to: LocalDateTime): List<StudyHistoryDto> {
        return studyHistoryRepository.findAllByUserAccountIdAndStartTimeBetweenOrderByStartTime(userId, from, to)
            .map { it.toDto() }
    }

    fun getUserDailyStatistics(day: LocalDateTime, userId: Long? = null): List<UserDailyDetailStatisticsDto> {
        val tempUserId = userId ?: userAccountService.getUserFromTheCurrentSession().id
        val startDay = day.truncatedTo(ChronoUnit.DAYS)
        val endDay = startDay.plusDays(1).minusNanos(1)
        val statistics =
            studyHistoryRepository.findAllByUserAccountIdAndStartTimeBetweenOrderByStartTime(
                tempUserId!!,
                startDay,
                endDay
            )

        return calculateUserDailyDetailStatistics(statistics)
    }

    fun getMonthHistoriesForCurrentUser(month: Int, year: Int): List<StudyHistoryDto> {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return getMonthHistories(currentUser.id!!, month, year)
    }

    fun getMonthHistories(userId: Long, month: Int, year: Int): List<StudyHistoryDto> {
        return studyHistoryRepository.getMonthHistories(userId, month, year)
            .map { it.toDto() }
    }

    fun isUserHasStatistics(userId: Long): Boolean {
        return studyHistoryRepository.isUserHasStatistics(userId)
    }

    private fun calculateUserDailyDetailStatistics(studyHistories: List<StudyHistory>):
        MutableList<UserDailyDetailStatisticsDto> {
            val result = mutableListOf<UserDailyDetailStatisticsDto>()
            studyHistories
                .groupBy { it.exercise.subGroup!!.series.name }
                .forEach { (seriesName, histories) ->
                    val allDoneExercisesCount = histories.size
                    val studyHistoryByExercise = histories
                        .groupBy { it.exercise.id }
                    val uniqueDoneExercisesCount = studyHistoryByExercise
                        .count()
                    val doneExercisesSuccessfullyFromFirstTime = studyHistoryByExercise
                        .count { it.value.size == 1 }
                    val listenWordsCount = histories.sumOf { it.tasksCount.toInt() }
                    val userDailyDetailStatisticsDto = UserDailyDetailStatisticsDto(
                        seriesName = seriesName,
                        allDoneExercises = allDoneExercisesCount,
                        uniqueDoneExercises = uniqueDoneExercisesCount,
                        doneExercisesSuccessfullyFromFirstTime = doneExercisesSuccessfullyFromFirstTime,
                        repeatedExercises = allDoneExercisesCount - doneExercisesSuccessfullyFromFirstTime,
                        listenWordsCount = listenWordsCount
                    )
                    result.add(userDailyDetailStatisticsDto)
                }
            return result
        }
}
