package com.epam.brn.service

import com.epam.brn.dto.SeriesDto
import com.epam.brn.exception.NoDataFoundException
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

    fun findSeriesForId(seriesId: Long): SeriesDto {
        log.debug("try to find series for seriesId=$seriesId")
        val series = seriesRepository.findById(seriesId)
            .orElseThrow { NoDataFoundException("no series was found for id=$seriesId") }
        return series.toDto()
    }
}
