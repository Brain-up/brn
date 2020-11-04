package com.epam.brn.service

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service
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
}
