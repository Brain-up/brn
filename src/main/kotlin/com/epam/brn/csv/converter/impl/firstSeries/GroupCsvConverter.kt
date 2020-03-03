package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.csv.converter.Converter
import com.epam.brn.csv.dto.GroupCsv
import com.epam.brn.model.ExerciseGroup
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import java.io.InputStream
import org.springframework.stereotype.Component

@Component
class GroupCsvConverter : Converter<GroupCsv, ExerciseGroup> {

    override fun iteratorProvider(): (InputStream) -> MappingIterator<GroupCsv> {
        val csvMapper = CsvMapper().apply {
            enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(GroupCsv::class.java)
            .withColumnSeparator(',')
            .withLineSeparator(" ")
            .withColumnReordering(true)
            .withHeader()

        return { file -> csvMapper
            .readerWithTypedSchemaFor(GroupCsv::class.java)
            .with(csvSchema)
            .readValues(file)
        }
    }

    override fun convert(source: GroupCsv) =
        ExerciseGroup(name = source.name, description = source.description, id = source.groupId)
}
