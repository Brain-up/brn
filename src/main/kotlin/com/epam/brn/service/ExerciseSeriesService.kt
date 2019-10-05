package com.epam.brn.service

import com.epam.brn.model.ExerciseSeries
import com.epam.brn.repo.ExerciseSeriesRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ExerciseSeriesService(@Autowired val exerciseSeriesRepository: ExerciseSeriesRepository) {

    private val log = logger()

    fun findUserDetails(name: String): List<ExerciseSeries> {
        return exerciseSeriesRepository.findAll() as List<ExerciseSeries>
    }
}