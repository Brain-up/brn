package com.epam.brn.csv.firstSeries.commaSeparated

import com.epam.brn.csv.dto.SeriesCsv
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.InputStream
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class CommaSeparatedSeriesCSVParserService : com.epam.brn.csv.CsvParser<SeriesCsv> {

    val log = logger()

    /**
     * @param file - csv-file which should be convert to model
     *
     * @return csv-line to pair of model object and error. One pair values must be empty.
     */

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
