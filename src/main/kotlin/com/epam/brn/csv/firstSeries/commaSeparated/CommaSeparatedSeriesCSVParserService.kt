package com.epam.brn.csv.firstSeries.commaSeparated

import com.epam.brn.csv.dto.SeriesCsv
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.InputStream
import org.apache.commons.lang3.StringUtils

class CommaSeparatedSeriesCSVParserService : com.epam.brn.csv.CsvParser<SeriesCsv> {

    override fun parseCsvFile(file: InputStream): MappingIterator<SeriesCsv> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(SeriesCsv::class.java)
            .withColumnSeparator(',')
            .withLineSeparator(StringUtils.SPACE)
            .withColumnReordering(true)
            .withHeader()

        return csvMapper
            .readerWithTypedSchemaFor(SeriesCsv::class.java)
            .with(csvSchema)
            .readValues(file)
    }
}
