package com.epam.brn.service

import com.epam.brn.dto.SeriesDto
import com.epam.brn.repo.SeriesRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SeriesService(@Autowired val seriesRepository: SeriesRepository) {

    private val log = logger()

    fun findSeries(userId: String): List<SeriesDto> {
        val series = seriesRepository.findAll()
        return series.map { seriesEntry -> seriesEntry.toDto() }
    }
}