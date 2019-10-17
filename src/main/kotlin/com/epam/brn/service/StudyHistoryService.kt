package com.epam.brn.service

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
    @Autowired val entityManager: EntityManager
) {
    private val log = logger()

    fun saveStudyHistory(studyHistoryDto: StudyHistoryDto): Long {
        val userReference = entityManager.getReference(UserAccount::class.java, studyHistoryDto.userId)
        val exerciseReference = entityManager.getReference(Exercise::class.java, studyHistoryDto.exerciseId)
        val savedStudyHistory = studyHistoryRepository.save(
            StudyHistory(
                id = 0,
                userAccount = userReference,
                exercise = exerciseReference,
                startTime = studyHistoryDto.startTime,
                endTime = studyHistoryDto.endTime,
                doneTasksCount = studyHistoryDto.doneTasksCount,
                successTasksCount = studyHistoryDto.successTasksCount,
                repetitionCount = studyHistoryDto.repetitionCount
            )
        )
        log.debug("Created new study story $savedStudyHistory")
        return savedStudyHistory.id
    }
}
