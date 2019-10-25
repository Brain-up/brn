package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
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

    fun findExercises(name: String): List<ExerciseDto> {
        val exercises = exerciseRepository.findByNameLike(name)
        return exercises.map { exercise -> exercise.toDto() }
    }

    fun findDoneExercises(userID: Long): List<ExerciseDto> {
        log.debug("Searching available exercises for $userID")
        val history = studyHistoryRepository.findByUserAccount_Id(userID)
        return emptyIfNull(history).mapNotNull { it.exercise }.map { it.toDto() }
    }
}