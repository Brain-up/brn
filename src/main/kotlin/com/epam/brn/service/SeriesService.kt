package com.epam.brn.service

import com.epam.brn.dto.SeriesDto
import com.epam.brn.repo.SeriesRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class SeriesService(private val seriesRepository: SeriesRepository) {

    private val log = logger()

    fun findSeriesForGroup(groupId: Long): List<SeriesDto> {
        val series = seriesRepository.findByExerciseGroupLike(groupId)
        log.info("try to find series for groupId=$groupId")
        return series.map { seriesEntry -> seriesEntry.toDto() }
    }
}