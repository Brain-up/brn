package com.epam.brn.service.parsers.csv.firstSeries.commaSeparated

import com.epam.brn.service.parsers.csv.dto.GroupCsv
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.InputStream
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class CommaSeparatedExerciseGroupCSVParserService : com.epam.brn.service.parsers.csv.CsvParser<GroupCsv> {

    val log = logger()

    /**
     * @param file - csv-file which should be convert to model
     *
     * @return csv-line to pair of model object and error. One pair values must be empty.
     */

    override fun parseCsvFile(file: InputStream): MappingIterator<GroupCsv> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(GroupCsv::class.java)
            .withColumnSeparator(',')
            .withLineSeparator(StringUtils.SPACE)
            .withColumnReordering(true)
            .withHeader()

        return csvMapper
            .readerWithTypedSchemaFor(GroupCsv::class.java)
            .with(csvSchema)
            .readValues<GroupCsv>(file)
    }
}
