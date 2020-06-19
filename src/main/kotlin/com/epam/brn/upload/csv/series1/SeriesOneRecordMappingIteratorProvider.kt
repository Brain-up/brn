package com.epam.brn.upload.csv.series1

import com.epam.brn.upload.csv.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.InputStream
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

@Component
class SeriesOneRecordMappingIteratorProvider :
    MappingIteratorProvider<SeriesOneRecord> {

    override fun iterator(inputStream: InputStream): MappingIterator<SeriesOneRecord> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(SeriesOneRecord::class.java)
            .withColumnReordering(true)
            .withArrayElementSeparator(StringUtils.SPACE)
            .withHeader()

        return csvMapper
            .readerFor(SeriesOneRecord::class.java)
            .with(csvSchema)
            .readValues(inputStream)
    }

    override fun isApplicable(format: String): Boolean {
        return SeriesOneRecord.FORMAT == format
    }
}
