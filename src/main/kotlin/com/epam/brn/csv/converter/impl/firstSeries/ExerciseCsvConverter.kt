package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.csv.converter.Converter
import com.epam.brn.csv.dto.ExerciseCsv
import com.epam.brn.model.Exercise
import com.epam.brn.service.SeriesService
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import java.io.InputStream
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ExerciseCsvConverter : Converter<ExerciseCsv, Exercise> {
    override fun iteratorProvider(): (InputStream) -> MappingIterator<ExerciseCsv> {
        val csvMapper = CsvMapper().apply {
            enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(ExerciseCsv::class.java)
            .withColumnSeparator(',')
            .withLineSeparator(" ")
            .withColumnReordering(true)
            .withHeader()

        return { file -> csvMapper
            .readerWithTypedSchemaFor(ExerciseCsv::class.java)
            .with(csvSchema)
            .readValues(file)
        }
    }

    @Autowired
    lateinit var seriesService: SeriesService

    override fun convert(source: ExerciseCsv): Exercise {
        val target = Exercise()
        convertSeries(source, target)
        convertExerciseType(source, target)
        target.name = source.name
        target.level = source.level
        target.description = source.description
        target.id = source.exerciseId
        return target
    }

    private fun convertSeries(source: ExerciseCsv, target: Exercise) {
        target.series = seriesService.findSeriesForId(source.seriesId)
    }

    private fun convertExerciseType(source: ExerciseCsv, target: Exercise) {
        val exerciseType =
            when (source.seriesId) {
                1L -> ExerciseTypeEnum.SINGLE_WORDS
                2L -> ExerciseTypeEnum.WORDS_SEQUENCES
                3L -> ExerciseTypeEnum.SENTENCE
                else -> throw IllegalArgumentException("There no ExerciseType for seriesId=${source.seriesId}")
            }
        target.exerciseType = exerciseType.toString()
    }
}
