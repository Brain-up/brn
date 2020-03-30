package com.epam.brn.upload.csv.series

import com.epam.brn.model.Series
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.service.ExerciseGroupsService
import com.epam.brn.upload.csv.RecordProcessor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SeriesGenericRecordProcessor(
    private val seriesRepository: SeriesRepository,
    private val exerciseGroupsService: ExerciseGroupsService
) : RecordProcessor<SeriesGenericRecord, Series> {

    override fun isApplicable(record: Any): Boolean {
        return record is SeriesGenericRecord
    }

    @Transactional
    override fun process(records: List<SeriesGenericRecord>): List<Series> {
        val result = records
            .map { Series(it, exerciseGroupsService.findGroupById(it.groupId)) }

        return seriesRepository.saveAll(result).toList()
    }
}
