package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import org.apache.commons.collections4.CollectionUtils.emptyIfNull
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ExerciseService(
    @Autowired val exerciseRepository: ExerciseRepository,
    @Autowired val studyHistoryRepository: StudyHistoryRepository
) {
    private val log = logger()

    fun findExercisesByID(exerciseID: Long): ExerciseDto {
        val exercise = exerciseRepository.findById(exerciseID)
        return exercise.map { e -> e.toDto() }
            .orElseThrow { NoDataFoundException("Could not find requested exerciseID=$exerciseID") }
    }

    fun findDoneExercises(userID: Long): List<ExerciseDto> {
        log.debug("Searching available exercises for $userID")
        val history = studyHistoryRepository.findByUserAccountId(userID)
        return emptyIfNull(history).mapNotNull { it.exercise }.map { it.toDto() }
    }
}