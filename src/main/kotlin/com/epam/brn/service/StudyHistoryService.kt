package com.epam.brn.service

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.integration.repo.ExerciseRepository
import com.epam.brn.integration.repo.StudyHistoryRepository
import com.epam.brn.integration.repo.UserAccountRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class StudyHistoryService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountRepository: UserAccountRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userAccountService: UserAccountService
) {
    private val log = logger()

    fun getTodayTimer(): Int {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return studyHistoryRepository.getTodayDayTimer(currentUser.id!!)
    }

    fun save(studyHistoryDto: StudyHistoryDto): StudyHistoryDto {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        val userAccount = userAccountRepository
            .findUserAccountById(currentUser.id!!)
            .orElseThrow { EntityNotFoundException("UserAccount with userId '${currentUser.id}' doesn't exist.") }
        val exercise = exerciseRepository
            .findById(studyHistoryDto.exerciseId)
            .orElseThrow { EntityNotFoundException("Exercise with exerciseId '${studyHistoryDto.exerciseId}' doesn't exist.") }
        val newStudyHistory = studyHistoryDto.toEntity(userAccount, exercise)
        // newStudyHistory.executionSeconds = calculateDiffInSeconds(studyHistoryDto.startTime, studyHistoryDto.endTime!!)
        val savedStudyHistory = studyHistoryRepository.save(newStudyHistory)

        return savedStudyHistory.toDto()
    }

    fun calculateDiffInSeconds(start: LocalDateTime, end: LocalDateTime): Int {
        return ChronoUnit.SECONDS.between(start, end).toInt()
    }

    fun getHistoriesForCurrentUser(from: LocalDate, to: LocalDate): List<StudyHistoryDto> {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return getHistories(currentUser.id!!, from, to)
    }

    fun getHistories(userId: Long, from: LocalDate, to: LocalDate): List<StudyHistoryDto> {
        return studyHistoryRepository.getHistories(userId, java.sql.Date.valueOf(from), java.sql.Date.valueOf(to))
            .map { it.toDto() }
    }

    fun getMonthHistoriesForCurrentUser(month: Int, year: Int): List<StudyHistoryDto> {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return getMonthHistories(currentUser.id!!, month, year)
    }

    fun getMonthHistories(userId: Long, month: Int, year: Int): List<StudyHistoryDto> {
        return studyHistoryRepository.getMonthHistories(userId, month, year)
            .map { it.toDto() }
    }
}
