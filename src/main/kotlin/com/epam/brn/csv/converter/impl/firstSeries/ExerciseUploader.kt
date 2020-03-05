package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.csv.converter.Uploader
import com.epam.brn.csv.dto.ExerciseCsv
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.service.SeriesService
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.springframework.stereotype.Component

@Component
class ExerciseUploader(
    private val exerciseRepository: ExerciseRepository,
    private val seriesService: SeriesService
) : Uploader<ExerciseCsv, Exercise> {

    override fun persistEntity(entity: Exercise) {
        exerciseRepository.save(entity)
    }

    override fun entityComparator(): (Exercise) -> Int {
        return { it.id?.toInt()!! }
    }

    override fun objectReader(): ObjectReader {
        val csvMapper = CsvMapper().apply {
            enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(ExerciseCsv::class.java)
            .withColumnSeparator(',')
            .withLineSeparator(" ")
            .withColumnReordering(true)
            .withHeader()

        return csvMapper
                .readerWithTypedSchemaFor(ExerciseCsv::class.java)
                .with(csvSchema)
    }

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
