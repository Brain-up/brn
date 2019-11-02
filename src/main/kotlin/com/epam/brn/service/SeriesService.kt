package com.epam.brn.service

import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.repo.SeriesRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class SeriesService(private val seriesRepository: SeriesRepository) {

    val EXERCISES = "exercises"
    private val log = logger()

    fun findSeriesForGroup(groupId: Long, include: String): List<Any> {
        log.debug("try to find series for groupId=$groupId")
        val series = seriesRepository.findByExerciseGroupLike(groupId)
        if (include == EXERCISES)
            return series.map { seriesEntry -> seriesEntry.toDtoWithFullExercises() }
        else
            return series.map { seriesEntry -> seriesEntry.toDtoWithExerciseIds() }
    }

    fun findSeriesForId(seriesId: Long, include: String): Any {
        log.debug("try to find series for seriesId=$seriesId")
        val series = seriesRepository.findById(seriesId)
            .orElseThrow { NoDataFoundException("no series was found for id=$seriesId") }
        if (include == EXERCISES)
            return series.toDtoWithFullExercises()
        else
            return series.toDtoWithExerciseIds()
    }
}