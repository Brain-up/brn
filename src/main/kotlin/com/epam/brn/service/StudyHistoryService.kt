package com.epam.brn.service

import com.epam.brn.converter.StudyHistoryConverter
import com.epam.brn.converter.StudyHistoryNotNullConverter
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.StudyHistoryRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.persistence.EntityManager

@Service
class StudyHistoryService(
    @Autowired val studyHistoryRepository: StudyHistoryRepository,
    @Autowired val entityManager: EntityManager,
    @Autowired val studyHistoryNotNullConverter: StudyHistoryNotNullConverter,
    @Autowired val studyHistoryConverter: StudyHistoryConverter
) {
        private val log = logger()
    fun saveOrReplaceStudyHistory(studyHistoryDto: StudyHistoryDto): Long? {
        val studyHistoryEntityOptional = studyHistoryRepository.findByUserAccount_IdAndExercise_Id(
            studyHistoryDto.userId,
            studyHistoryDto.exerciseId
        )
        if (studyHistoryEntityOptional.isPresent) {
            log.debug("Replacing $studyHistoryDto")
            val studyHistoryEntity = studyHistoryEntityOptional.get()
            return updateEntity(studyHistoryDto, studyHistoryEntity)
        }
        log.debug("Saving $studyHistoryDto")
        val userReference = entityManager.getReference(UserAccount::class.java, studyHistoryDto.userId)
        val exerciseReference = entityManager.getReference(Exercise::class.java, studyHistoryDto.exerciseId)
        return studyHistoryRepository.save(
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

    fun replaceStudyHistory(studyHistoryDto: StudyHistoryDto): Long? {
        log.debug("Replacing $studyHistoryDto")
        val studyHistoryEntity = studyHistoryRepository.findByUserAccount_IdAndExercise_Id(
            studyHistoryDto.userId,
            studyHistoryDto.exerciseId
        ).get()
        return updateEntity(studyHistoryDto, studyHistoryEntity)
    }

    fun patchStudyHistory(studyHistoryDto: StudyHistoryDto): Long? {
        log.debug("Patching $studyHistoryDto")
        val studyHistoryEntity = studyHistoryRepository.findByUserAccount_IdAndExercise_Id(
            studyHistoryDto.userId,
            studyHistoryDto.exerciseId
        ).get()
        return updateEntityNotNullOnly(studyHistoryDto, studyHistoryEntity)
    }

    private fun updateEntityNotNullOnly(
        studyHistoryDto: StudyHistoryDto,
        studyHistoryEntity: StudyHistory
    ): Long? {
        studyHistoryNotNullConverter
            .updateStudyHistory(studyHistoryDto, studyHistoryEntity)
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
