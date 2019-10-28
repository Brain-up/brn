package com.epam.brn.service

import com.epam.brn.dto.SeriesDto
import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.repo.SeriesRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class SeriesService(private val seriesRepository: SeriesRepository) {

    private val log = logger()

    fun findSeriesForGroup(groupId: Long, include: String): List<SeriesDto> {
        log.debug("try to find series for groupId=$groupId")
        val series = seriesRepository.findByExerciseGroupLike(groupId)
        if ("exercises" == include)
            return series.map { seriesEntry -> seriesEntry.toDtoWithExercises() }
        else
            return series.map { seriesEntry -> seriesEntry.toDtoWithoutExercises() }
    }

    fun findSeriesForId(seriesId: Long): SeriesDto {
        log.debug("try to find series for seriesId=$seriesId")
        val series = seriesRepository.findById(seriesId)
            .orElseGet { throw NoDataFoundException("no series was found for id=$seriesId") }
        return series.toDtoWithoutExercises()
    }
}