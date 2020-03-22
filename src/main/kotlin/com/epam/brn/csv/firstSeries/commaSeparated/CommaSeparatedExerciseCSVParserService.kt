package com.epam.brn.csv.firstSeries.commaSeparated

import com.epam.brn.csv.dto.ExerciseCsv
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.InputStream
import org.apache.commons.lang3.StringUtils

class CommaSeparatedExerciseCSVParserService : com.epam.brn.csv.CsvParser<ExerciseCsv> {

    override fun iterator(file: InputStream): MappingIterator<ExerciseCsv> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(ExerciseCsv::class.java)
            .withColumnSeparator(',')
            .withLineSeparator(StringUtils.SPACE)
            .withColumnReordering(true)
            .withHeader()

        return csvMapper
            .readerWithTypedSchemaFor(ExerciseCsv::class.java)
            .with(csvSchema)
            .readValues(file)
    }
}
