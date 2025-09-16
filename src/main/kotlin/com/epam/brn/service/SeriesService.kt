package com.epam.brn.service

import com.epam.brn.dto.SeriesDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.repo.SeriesRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class SeriesService(
    private val seriesRepository: SeriesRepository,
) {
    private val log = logger()

    @Cacheable("series")
    fun findSeriesForGroup(groupId: Long): List<SeriesDto> {
        log.debug("try to find active series for groupId=$groupId")
        val series =
            seriesRepository
                .findDistinctByExerciseGroupIdAndActiveTrue(groupId)
        return series.map { seriesEntry -> seriesEntry.toDto() }
    }

    @Cacheable("seriesDto")
    fun findSeriesDtoForId(seriesId: Long): SeriesDto {
        log.debug("try to find active series for seriesId=$seriesId")
        val series =
            seriesRepository
                .findDistinctByIdAndActiveTrue(seriesId)
                .orElseThrow { EntityNotFoundException("no active series was found for id=$seriesId") }
        return series.toDto()
    }
}
