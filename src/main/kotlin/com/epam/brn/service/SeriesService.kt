package com.epam.brn.service

import com.epam.brn.dto.SeriesDto
import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.model.Series
import com.epam.brn.repo.SeriesRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class SeriesService(private val seriesRepository: SeriesRepository) {

    private val log = logger()

    fun findSeriesForGroup(groupId: Long): List<SeriesDto> {
        log.debug("try to find series for groupId=$groupId")
        val series = seriesRepository.findByExerciseGroupLike(groupId)
        return series.map { seriesEntry -> seriesEntry.toDto() }
    }

    fun findSeriesDtoForId(seriesId: Long): SeriesDto {
        log.debug("try to find series for seriesId=$seriesId")
        val series = seriesRepository.findById(seriesId)
            .orElseThrow { NoDataFoundException("no series was found for id=$seriesId") }
        return series.toDto()
    }

    fun findSeriesForId(seriesId: Long): Series {
        log.debug("try to find series for seriesId=$seriesId")
        return seriesRepository.findById(seriesId)
            .orElseThrow { NoDataFoundException("no series was found for id=$seriesId") }
    }

    fun findSeriesWithExercisesForId(seriesId: Long): Series {
        log.debug("try to find series for seriesId=$seriesId")
        return seriesRepository.findSeriesWithExercisesById(seriesId)
            .orElseThrow { NoDataFoundException("no series was found for id=$seriesId") }
    }

    fun save(series: Series): Series {
        return seriesRepository.save(series)
    }
}
