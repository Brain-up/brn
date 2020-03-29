package com.epam.brn.upload.csv.parser.iterator.impl

import com.epam.brn.upload.csv.parser.iterator.MappingIteratorProvider
import com.epam.brn.upload.csv.record.SeriesOneRecord
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import java.io.InputStream
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

@Component
class SeriesOneRecordMappingIteratorProvider :
    MappingIteratorProvider<SeriesOneRecord> {

    override fun iterator(inputStream: InputStream): MappingIterator<SeriesOneRecord> {
        val csvMapper = CsvMapper()

        val csvSchema = csvMapper
            .schemaFor(SeriesOneRecord::class.java)
            .withColumnSeparator(' ')
            .withLineSeparator(StringUtils.SPACE)
            .withColumnReordering(true)
            .withArrayElementSeparator(",")
            .withHeader()

        return csvMapper
            .readerWithTypedSchemaFor(SeriesOneRecord::class.java)
            .with(csvSchema)
            .readValues(inputStream)
    }

    override fun isApplicable(format: String): Boolean {
        return SeriesOneRecord.FORMAT == format
    }
}
