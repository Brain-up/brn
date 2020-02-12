package com.epam.brn.service.parsers.csv.firstSeries.commaSeparated

import com.epam.brn.service.parsers.csv.dto.ExerciseCsv
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.InputStream
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class CommaSeparatedExerciseCSVParserService : com.epam.brn.service.parsers.csv.CsvParser<ExerciseCsv> {

    val log = logger()

    /**
     * @param file - csv-file which should be convert to model
     *
     * @return csv-line to pair of model object and error. One pair values must be empty.
     */

    override fun parseCsvFile(file: InputStream): MappingIterator<ExerciseCsv> {
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
            .readValues<ExerciseCsv>(file)
    }
}
