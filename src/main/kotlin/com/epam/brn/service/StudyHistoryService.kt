package com.epam.brn.service

import com.epam.brn.converter.StudyHistoryConverter
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
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
    private val studyHistoryConverter: StudyHistoryConverter,
    private val userAccountService: UserAccountService
) {
    private val log = logger()

    fun getTodayTimer(): Int {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return studyHistoryRepository.getTodayDayTimer(currentUser.id!!)
    }

    fun save(studyHistoryDto: StudyHistoryDto): StudyHistoryDto {
        return create(studyHistoryDto)
    }

    private fun update(studyHistory: StudyHistory, studyHistoryDto: StudyHistoryDto): StudyHistoryDto {
        studyHistoryConverter.updateStudyHistory(studyHistoryDto, studyHistory)
        studyHistoryRepository.save(studyHistory)
        return studyHistory.toDto()
    }

    private fun create(studyHistoryDto: StudyHistoryDto): StudyHistoryDto {
        val userAccount = findUserAccount(studyHistoryDto.userId)
        val exercise = findExercise(studyHistoryDto.exerciseId)

        val newStudyHistory = StudyHistory(userAccount = userAccount, exercise = exercise)
        val studyHistory = studyHistoryConverter.updateStudyHistory(studyHistoryDto, newStudyHistory)
        studyHistory.executionSeconds = calculateDiffInSeconds(studyHistoryDto.startTime!!, studyHistoryDto.endTime!!)
        val savedEntity = studyHistoryRepository.save(studyHistory)

        return savedEntity.toDto()
    }

    fun calculateDiffInSeconds(start: LocalDateTime, end: LocalDateTime): Int {
        return ChronoUnit.SECONDS.between(start, end).toInt()
    }

    private fun findUserAccount(id: Long): UserAccount {
        return userAccountRepository
            .findUserAccountById(id)
            .orElseThrow { EntityNotFoundException("UserAccount with userId '$id' doesn't exist.") }
    }

    private fun findExercise(exerciseId: Long): Exercise {
        return exerciseRepository
            .findById(exerciseId)
            .orElseThrow {
                EntityNotFoundException("Exercise with exerciseId '$exerciseId' doesn't exist.")
            }
    }
}
