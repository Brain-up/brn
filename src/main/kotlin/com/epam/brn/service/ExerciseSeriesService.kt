package com.epam.brn.service

import com.epam.brn.dto.SeriesDto
import com.epam.brn.model.ExerciseSeries
import com.epam.brn.repo.ExerciseSeriesRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ExerciseSeriesService(@Autowired val exerciseSeriesRepository: ExerciseSeriesRepository) {

    private val log = logger()

    fun findSeries(userId: String): List<SeriesDto> {
        // todo find users avaliable series
        val series: List<ExerciseSeries> = exerciseSeriesRepository.findAll() as List<ExerciseSeries>
        // todo use mapping from Andrey
        val seriesDtos =
            listOf(SeriesDto(series[0].id.toString(), series[0].name), SeriesDto(series[1].id.toString(), series[1].name))
        return seriesDtos
    }
}