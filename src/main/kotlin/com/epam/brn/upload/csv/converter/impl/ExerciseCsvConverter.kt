package com.epam.brn.upload.csv.converter.impl

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.model.Exercise
import com.epam.brn.service.SeriesService
import com.epam.brn.upload.csv.converter.Converter
import com.epam.brn.upload.csv.dto.ExerciseCsv
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ExerciseCsvConverter : Converter<ExerciseCsv, Exercise> {

    @Autowired
    lateinit var seriesService: SeriesService

    override fun convert(source: ExerciseCsv): Exercise {
        val target = Exercise()
        target.series = seriesService.findSeriesForId(source.seriesId)
        target.exerciseType = ExerciseTypeEnum.of(source.seriesId).toString()
        target.name = source.name
        target.level = source.level
        target.description = source.description
        target.id = source.exerciseId
        return target
    }
}
