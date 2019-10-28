package com.epam.brn.service

import com.epam.brn.converter.StudyHistoryConverter
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.StudyHistoryRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.security.InvalidParameterException
import javax.persistence.EntityManager

@Service
class StudyHistoryService(
    @Autowired val studyHistoryRepository: StudyHistoryRepository,
    @Autowired val entityManager: EntityManager,
    @Autowired val studyHistoryConverter: StudyHistoryConverter
) {
    private val log = logger()
    fun saveOrReplaceStudyHistory(studyHistoryDto: StudyHistoryDto): Long? {
        return studyHistoryRepository.findByUserAccountIdAndExerciseId(
            studyHistoryDto.userId,
            studyHistoryDto.exerciseId
        ).map { studyHistoryEntity ->
            log.debug("Replacing $studyHistoryDto")
            updateEntity(studyHistoryDto, studyHistoryEntity)
        }.orElseGet {
            log.debug("Saving $studyHistoryDto")
            val userReference = entityManager.getReference(UserAccount::class.java, studyHistoryDto.userId)
            val exerciseReference = entityManager.getReference(Exercise::class.java, studyHistoryDto.exerciseId)
            studyHistoryRepository.save(
                StudyHistory(
                    userAccount = userReference,
                    exercise = exerciseReference,
                    startTime = studyHistoryDto.startTime,
                    endTime = studyHistoryDto.endTime,
                    doneTasksCount = studyHistoryDto.doneTasksCount,
                    successTasksCount = studyHistoryDto.successTasksCount,
                    repetitionCount = studyHistoryDto.repetitionCount
                )
            ).id
        }
    }

    @Throws(InvalidParameterException::class)
    fun replaceStudyHistory(studyHistoryDto: StudyHistoryDto): Long? {
        log.debug("Replacing $studyHistoryDto")
        return studyHistoryRepository.findByUserAccountIdAndExerciseId(
            studyHistoryDto.userId,
            studyHistoryDto.exerciseId
        ).map { studyHistoryEntity -> updateEntity(studyHistoryDto, studyHistoryEntity) }
            .orElseThrow { InvalidParameterException("Could not find requested study history") }
    }

    @Throws(InvalidParameterException::class)
    fun patchStudyHistory(studyHistoryDto: StudyHistoryDto): Long? {
        log.debug("Patching $studyHistoryDto")
        return studyHistoryRepository.findByUserAccountIdAndExerciseId(
            studyHistoryDto.userId,
            studyHistoryDto.exerciseId
        ).map { studyHistoryEntity -> updateEntityNotNullOnly(studyHistoryDto, studyHistoryEntity) }
            .orElseThrow { InvalidParameterException("Could not find requested study history") }
    }

    private fun updateEntityNotNullOnly(
        studyHistoryDto: StudyHistoryDto,
        studyHistoryEntity: StudyHistory
    ): Long? {
        studyHistoryConverter
            .updateStudyHistoryWhereNotNull(studyHistoryDto, studyHistoryEntity)
        return studyHistoryRepository.save(studyHistoryEntity).id
    }

    private fun updateEntity(
        studyHistoryDto: StudyHistoryDto,
        studyHistoryEntity: StudyHistory
    ): Long? {
        studyHistoryConverter
            .updateStudyHistory(studyHistoryDto, studyHistoryEntity)
        return studyHistoryRepository.save(studyHistoryEntity).id
    }
}
