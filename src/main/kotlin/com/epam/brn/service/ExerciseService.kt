package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.Optional

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

    fun findAvailableExercises(userID: Long): List<ExerciseDto> {
        log.debug("Searching available exercises for $userID")
        val history = studyHistoryRepository.findByUserAccount_Id(userID)
        return history.map { h -> Optional.ofNullable(h.exercise).map { e -> e.toDto() }.orElse(null) }
    }
}