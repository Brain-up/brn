package com.epam.brn.service.parsers.csv.firstSeries

import com.epam.brn.service.parsers.csv.CsvParser
import com.epam.brn.service.parsers.csv.dto.TaskCsv
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import java.io.InputStream
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class TaskCSVParser1SeriesService : CsvParser<TaskCsv> {

    val log = logger()

    /**
     * @param file - csv-file which should be convert to model
     *
     * @return csv-line to pair of model object and error. One pair values must be empty.
     */
    override fun parseCsvFile(file: InputStream): MappingIterator<TaskCsv> {
        val csvMapper = CsvMapper()

        val csvSchema = csvMapper
            .schemaFor(TaskCsv::class.java)
            .withColumnSeparator(' ')
            .withLineSeparator(StringUtils.SPACE)
            .withColumnReordering(true)
            .withArrayElementSeparator(",")
            .withHeader()

        return csvMapper
            .readerWithTypedSchemaFor(TaskCsv::class.java)
            .with(csvSchema)
            .readValues<TaskCsv>(file)
    }
}
