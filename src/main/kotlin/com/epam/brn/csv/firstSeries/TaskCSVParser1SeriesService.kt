package com.epam.brn.csv.firstSeries

import com.epam.brn.csv.CsvParser
import com.epam.brn.csv.dto.TaskCsv
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import java.io.InputStream
import org.apache.commons.lang3.StringUtils

class TaskCSVParser1SeriesService : CsvParser<TaskCsv> {

    override fun iterator(file: InputStream): MappingIterator<TaskCsv> {
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
            .readValues(file)
    }
}
