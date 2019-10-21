package com.epam.brn.service

import com.epam.brn.converter.StudyHistoryConverter
import com.epam.brn.converter.StudyHistoryNotNullConverter
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.StudyHistoryRepository
import org.mapstruct.factory.Mappers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.persistence.EntityManager

@Service
class StudyHistoryService(
    @Autowired val studyHistoryRepository: StudyHistoryRepository,
    @Autowired val entityManager: EntityManager
) {
    fun saveStudyHistory(studyHistoryDto: StudyHistoryDto): Long? {
        val userReference = entityManager.getReference(UserAccount::class.java, studyHistoryDto.userId)
        val exerciseReference = entityManager.getReference(Exercise::class.java, studyHistoryDto.exerciseId)
        val studyHistoryEntityOptional = studyHistoryRepository.findByUserAccount_IdAndExercise_Id(
            studyHistoryDto.userId,
            studyHistoryDto.exerciseId
        )
        if (studyHistoryEntityOptional.isPresent) {
            val studyHistoryEntity = studyHistoryEntityOptional.get()
            return updateEntity(studyHistoryDto, studyHistoryEntity)
        }
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
        val studyHistoryEntity = studyHistoryRepository.findByUserAccount_IdAndExercise_Id(
            studyHistoryDto.userId,
            studyHistoryDto.exerciseId
        ).get()
        return updateEntity(studyHistoryDto, studyHistoryEntity)
    }

    fun patchStudyHistory(studyHistoryDto: StudyHistoryDto): Long? {
        val studyHistoryEntity = studyHistoryRepository.findByUserAccount_IdAndExercise_Id(
            studyHistoryDto.userId,
            studyHistoryDto.exerciseId
        ).get()
        return updateEntityNotNull(studyHistoryDto, studyHistoryEntity)
    }

    private fun updateEntityNotNull(
        studyHistoryDto: StudyHistoryDto,
        studyHistoryEntity: StudyHistory
    ): Long? {
        Mappers.getMapper(StudyHistoryNotNullConverter::class.java)
            .updateStudyHistory(studyHistoryDto, studyHistoryEntity)
        return studyHistoryRepository.save(studyHistoryEntity).id
    }

    private fun updateEntity(
        studyHistoryDto: StudyHistoryDto,
        studyHistoryEntity: StudyHistory
    ): Long? {
        Mappers.getMapper(StudyHistoryConverter::class.java)
            .updateStudyHistory(studyHistoryDto, studyHistoryEntity)
        return studyHistoryRepository.save(studyHistoryEntity).id
    }
}
