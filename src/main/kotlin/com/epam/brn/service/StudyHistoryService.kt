package com.epam.brn.service

import com.epam.brn.converter.StudyHistoryConverter
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import java.security.InvalidParameterException
import java.util.Optional
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class StudyHistoryService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val exerciseRepository: ExerciseRepository,
    private val studyHistoryConverter: StudyHistoryConverter
) {
    private val log = logger()

    fun findBy(userId: String?, exerciseId: Long?): Optional<StudyHistory> {
        return studyHistoryRepository.findByUserIdAndExerciseId(userId, exerciseId)
    }

    fun update(studyHistory: StudyHistory, studyHistoryDto: StudyHistoryDto): StudyHistoryDto {
        studyHistoryConverter.updateStudyHistory(studyHistoryDto, studyHistory)
        studyHistoryRepository.save(studyHistory)
        return studyHistory.toDto()
    }

    fun create(studyHistoryDto: StudyHistoryDto): StudyHistoryDto {
        val exercise = findExercise(studyHistoryDto.exerciseId!!)

        val newStudyHistory = StudyHistory(userId = "userAccount", exercise = exercise)
        val studyHistory = studyHistoryConverter.updateStudyHistory(studyHistoryDto, newStudyHistory)
        studyHistoryRepository.save(studyHistory)

        return studyHistory.toDto()
    }

    private fun findExercise(exerciseId: Long): Exercise {
        return exerciseRepository
            .findById(exerciseId)
            .orElseThrow {
                EntityNotFoundException("Exercise with exerciseId '$exerciseId' doesn't exist.")
            }
    }

    @Throws(InvalidParameterException::class)
    fun patchStudyHistory(studyHistoryDto: StudyHistoryDto): StudyHistoryDto {
        log.debug("Patching $studyHistoryDto")
        return studyHistoryRepository
            .findByUserIdAndExerciseId(
                studyHistoryDto.userId,
                studyHistoryDto.exerciseId
            ).map { studyHistoryEntity ->
                studyHistoryConverter.updateStudyHistoryWhereNotNull(studyHistoryDto, studyHistoryEntity)
                studyHistoryRepository.save(studyHistoryEntity).toDto()
            }.orElseThrow {
                EntityNotFoundException("Could not find requested study history with id=${studyHistoryDto.id}")
            }
    }
}
