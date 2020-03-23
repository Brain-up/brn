package com.epam.brn.upload.csv.converter.impl

import com.epam.brn.model.Series
import com.epam.brn.service.ExerciseGroupsService
import com.epam.brn.upload.csv.converter.Converter
import com.epam.brn.upload.csv.record.SeriesRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SeriesCsvConverter : Converter<SeriesRecord, Series> {

    @Autowired
    lateinit var exerciseGroupsService: ExerciseGroupsService

    override fun convert(source: SeriesRecord): Series {
        return Series(
            name = source.name,
            description = source.description,
            exerciseGroup = exerciseGroupsService.findGroupById(source.groupId),
            id = source.seriesId
        )
    }
}
