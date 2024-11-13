package com.epam.brn.service

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.statistics.UserDailyDetailStatisticsDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
class StudyHistoryService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userAccountService: UserAccountService
) {

    fun getTodayTimer(): Int {
        val currentUser = userAccountService.getCurrentUserDto()
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

    fun getHistoriesForCurrentUser(from: LocalDateTime, to: LocalDateTime): List<StudyHistoryDto> {
        val currentUser = userAccountService.getCurrentUserDto()
        return getHistories(currentUser.id!!, from, to)
    }

    fun getHistories(userId: Long, from: LocalDateTime, to: LocalDateTime): List<StudyHistoryDto> {
        return studyHistoryRepository.getHistories(userId, from, to)
            .map { it.toDto() }
    }

    fun getUserDailyStatistics(day: LocalDateTime, userId: Long? = null): List<UserDailyDetailStatisticsDto> {
        val tempUserId = userId ?: userAccountService.getCurrentUserDto().id
        val startDay = day.truncatedTo(ChronoUnit.DAYS)
        val endDay = startDay.plusDays(1).minusNanos(1)
        val statistics =
            studyHistoryRepository.getHistories(
                tempUserId!!,
                startDay,
                endDay
            )

        return calculateUserDailyDetailStatistics(statistics)
    }

    fun getMonthHistoriesForCurrentUser(month: Int, year: Int): List<StudyHistoryDto> {
        val currentUser = userAccountService.getCurrentUserDto()
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
            return studyHistories
                .groupBy { it.exercise.subGroup!!.series.name }
                .map { (seriesName, histories) ->
                    val exerciseGroups = histories.groupBy { it.exercise.id }
                    UserDailyDetailStatisticsDto(
                        seriesName = seriesName,
                        allDoneExercises = histories.size,
                        uniqueDoneExercises = exerciseGroups.size,
                        doneExercisesSuccessfullyFromFirstTime = exerciseGroups.count { it.value.size == 1 },
                        repeatedExercises = histories.size - exerciseGroups.count { it.value.size == 1 },
                        listenWordsCount = histories.sumOf { it.tasksCount.toInt() },
                        duration = (histories.sumOf { it.spentTimeInSeconds ?: 0L }.toDouble() / 60)
                            .toDuration(DurationUnit.MINUTES)
                    )
                }
                .toMutableList()
        }
}
