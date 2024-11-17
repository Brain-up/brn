package com.epam.brn.service

import com.epam.brn.dto.SeriesDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.repo.SeriesRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class SeriesService(private val seriesRepository: SeriesRepository) {

    private val log = logger()

    @Cacheable("series")
    fun findSeriesForGroup(groupId: Long): List<SeriesDto> {
        log.debug("try to find series for groupId=$groupId")
        val series = seriesRepository.findByExerciseGroupLike(groupId)
        return series.map { seriesEntry -> seriesEntry.toDto() }
    }

    @Cacheable("seriesDto")
    fun findSeriesDtoForId(seriesId: Long): SeriesDto {
        log.debug("try to find series for seriesId=$seriesId")
        val series = seriesRepository.findById(seriesId)
            .orElseThrow { EntityNotFoundException("no series was found for id=$seriesId") }
        return series.toDto()
    }
}
