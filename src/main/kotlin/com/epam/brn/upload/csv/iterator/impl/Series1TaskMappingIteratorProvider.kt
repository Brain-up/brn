package com.epam.brn.upload.csv.iterator.impl

import com.epam.brn.upload.csv.dto.TaskCsv
import com.epam.brn.upload.csv.iterator.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import java.io.InputStream
import org.apache.commons.lang3.StringUtils

class Series1TaskMappingIteratorProvider :
    MappingIteratorProvider<TaskCsv> {

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
