package com.epam.brn.upload.csv.iterator.impl

import com.epam.brn.upload.csv.iterator.MappingIteratorProvider
import com.epam.brn.upload.csv.record.SeriesOneTaskRecord
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import java.io.InputStream
import org.apache.commons.lang3.StringUtils

class Series1TaskMappingIteratorProvider :
    MappingIteratorProvider<SeriesOneTaskRecord> {

    override fun iterator(file: InputStream): MappingIterator<SeriesOneTaskRecord> {
        val csvMapper = CsvMapper()

        val csvSchema = csvMapper
            .schemaFor(SeriesOneTaskRecord::class.java)
            .withColumnSeparator(' ')
            .withLineSeparator(StringUtils.SPACE)
            .withColumnReordering(true)
            .withArrayElementSeparator(",")
            .withHeader()

        return csvMapper
            .readerWithTypedSchemaFor(SeriesOneTaskRecord::class.java)
            .with(csvSchema)
            .readValues(file)
    }
}
