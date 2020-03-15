package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.csv.converter.Uploader
import com.epam.brn.csv.dto.SeriesCsv
import com.epam.brn.model.Series
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.service.ExerciseGroupsService
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.springframework.stereotype.Component

@Component
class SeriesUploader(
    private val seriesRepository: SeriesRepository,
    private val exerciseGroupsService: ExerciseGroupsService
) : Uploader<SeriesCsv, Series> {

    override fun shouldProcess(fileName: String) = "series.csv" == fileName

    override fun save(entity: Series) {
        seriesRepository.save(entity)
    }

    override fun objectReader(): ObjectReader {
        val csvMapper = CsvMapper().apply {
            enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(SeriesCsv::class.java)
            .withColumnSeparator(',')
            .withLineSeparator(" ")
            .withColumnReordering(true)
            .withHeader()

        return csvMapper
                .readerWithTypedSchemaFor(SeriesCsv::class.java)
                .with(csvSchema)
    }

    override fun convert(source: SeriesCsv): Series {
        return Series(
            name = source.name,
            description = source.description,
            exerciseGroup = exerciseGroupsService.findGroupById(source.groupId),
            id = source.seriesId
        )
    }
}
