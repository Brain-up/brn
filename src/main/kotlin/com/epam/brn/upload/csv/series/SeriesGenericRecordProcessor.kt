package com.epam.brn.upload.csv.series

import com.epam.brn.enums.Locale
import com.epam.brn.model.Series
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.service.ExerciseGroupsService
import com.epam.brn.upload.csv.RecordProcessor
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SeriesGenericRecordProcessor(
    private val seriesRepository: SeriesRepository,
    private val exerciseGroupsService: ExerciseGroupsService
) : RecordProcessor<SeriesGenericRecord, Series> {

    private val log = logger()

    override fun isApplicable(record: Any): Boolean {
        return record is SeriesGenericRecord
    }

    @Transactional
    override fun process(records: List<SeriesGenericRecord>, locale: Locale): List<Series> {
        val series = records
            .map { Series(it, exerciseGroupsService.findGroupById(it.groupId)) }
        series.forEach { series ->
            run {
                val existSeries = seriesRepository.findByTypeAndName(series.type, series.name)
                if (existSeries == null)
                    seriesRepository.save(series)
            }
        }
        return series
    }
}
