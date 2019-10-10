package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.repo.ExerciseRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ExerciseService(@Autowired val exerciseRepository: ExerciseRepository) {

    private val log = logger()

    fun findExercises(exerciseSeriesId: String, userId: String): List<ExerciseDto> {
        // todo: get from db
        // exerciseRepository.findByExerciseSeriesIdLike(exerciseSeriesId) and then make calculation for user
        return listOf(
            ExerciseDto("1", "однослоговые слова", "1"),
            ExerciseDto("2", "двуслоговые слова слова", "1"),
            ExerciseDto("3", "сложные слова слова", "1"))
    }
}