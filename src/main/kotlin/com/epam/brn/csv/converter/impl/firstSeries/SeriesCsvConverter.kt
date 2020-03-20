package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.csv.converter.Converter
import com.epam.brn.csv.dto.SeriesCsv
import com.epam.brn.model.Series
import com.epam.brn.service.ExerciseGroupsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SeriesCsvConverter : Converter<SeriesCsv, Series> {

    @Autowired
    lateinit var exerciseGroupsService: ExerciseGroupsService

    override fun convert(source: SeriesCsv): Series {
        return Series(
            name = source.name,
            description = source.description,
            exerciseGroup = exerciseGroupsService.findGroupById(source.groupId),
            id = source.seriesId
        )
    }
}
