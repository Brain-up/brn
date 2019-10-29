package com.epam.brn.service

import com.epam.brn.dto.SeriesDto
import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.repo.SeriesRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class SeriesService(private val seriesRepository: SeriesRepository) {

    val EXERCISES = "exercises"
    private val log = logger()

    fun findSeriesForGroup(groupId: Long, include: String): List<SeriesDto> {
        log.debug("try to find series for groupId=$groupId")
        val series = seriesRepository.findByExerciseGroupLike(groupId)
        var listDto: List<SeriesDto>
        if (include == EXERCISES)
            listDto = series.map { seriesEntry -> seriesEntry.toDtoWithExercises() }
        else
            listDto = series.map { seriesEntry -> seriesEntry.toDtoWithoutExercises() }
        return listDto
    }

    fun findSeriesForId(seriesId: Long, include: String): SeriesDto {
        log.debug("try to find series for seriesId=$seriesId")
        val series = seriesRepository.findById(seriesId)
            .orElseThrow { NoDataFoundException("no series was found for id=$seriesId") }
        if (include == EXERCISES)
            return series.toDtoWithExercises()
        else
            return series.toDtoWithoutExercises()
    }
}