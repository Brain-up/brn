package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.repo.ExerciseRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ExerciseService(@Autowired val exerciseRepository: ExerciseRepository) {

    private val log = logger()

    fun findExercises(name: String): List<ExerciseDto> {
        val exercises = exerciseRepository.findByNameLike(name)
        return exercises.map { exercise -> exercise.toDtoWithTasks() }
    }
}