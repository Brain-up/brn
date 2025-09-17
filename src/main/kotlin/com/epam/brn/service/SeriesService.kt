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
        return seriesRepository
            .findDistinctByExerciseGroupIdAndActiveTrue(groupId)
            .map { it.toDto() }
    }

    @Cacheable("seriesDto")
    fun findSeriesDtoForId(seriesId: Long): SeriesDto {
        log.debug("try to find active series for seriesId=$seriesId")
        return seriesRepository
            .findByIdAndActiveTrue(seriesId)
            ?.toDto()
            ?: throw EntityNotFoundException("no active series was found for id=$seriesId")
    }
}
