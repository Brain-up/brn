package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.csv.converter.Converter
import com.epam.brn.csv.dto.SeriesCsv
import com.epam.brn.model.Series
import com.epam.brn.service.ExerciseGroupsService
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import java.io.InputStream
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SeriesCsvConverter : Converter<SeriesCsv, Series> {

    @Autowired
    lateinit var exerciseGroupsService: ExerciseGroupsService

    override fun iteratorProvider(): (InputStream) -> MappingIterator<SeriesCsv> {
        val csvMapper = CsvMapper().apply {
            enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(SeriesCsv::class.java)
            .withColumnSeparator(',')
            .withLineSeparator(" ")
            .withColumnReordering(true)
            .withHeader()

        return { file -> csvMapper
            .readerWithTypedSchemaFor(SeriesCsv::class.java)
            .with(csvSchema)
            .readValues(file)
        }
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
