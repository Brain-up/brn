package com.epam.brn.upload.csv.processor

import com.epam.brn.model.Series
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.service.ExerciseGroupsService
import com.epam.brn.upload.csv.record.SeriesGenericRecord
import org.springframework.stereotype.Component

@Component
class SeriesGenericRecordProcessor(
    private val seriesRepository: SeriesRepository,
    private val exerciseGroupsService: ExerciseGroupsService
) {

    fun process(records: List<SeriesGenericRecord>): List<Series> {
        val result = records
            .map { Series(it, exerciseGroupsService.findGroupById(it.groupId)) }

        return seriesRepository.saveAll(result).toList()
    }
}
