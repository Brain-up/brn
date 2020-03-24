package com.epam.brn.upload.csv.processor

import com.epam.brn.model.Series
import com.epam.brn.service.ExerciseGroupsService
import com.epam.brn.upload.csv.record.SeriesGenericRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SeriesGenericRecordProcessor {

    @Autowired
    lateinit var exerciseGroupsService: ExerciseGroupsService

    fun convert(source: SeriesGenericRecord): Series {
        return Series(
            name = source.name,
            description = source.description,
            exerciseGroup = exerciseGroupsService.findGroupById(source.groupId),
            id = source.seriesId
        )
    }
}
