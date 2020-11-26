package com.epam.brn.upload.csv.series4

import com.epam.brn.upload.csv.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class SeriesFourRecordMappingIteratorProvider :
    MappingIteratorProvider<SeriesFourRecord> {

    override fun iterator(inputStream: InputStream): MappingIterator<SeriesFourRecord> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(SeriesFourRecord::class.java)
            .withColumnReordering(true)
            .withArrayElementSeparator(StringUtils.SPACE)
            .withHeader()

        return csvMapper
            .readerFor(SeriesFourRecord::class.java)
            .with(csvSchema)
            .readValues(inputStream)
    }

    override fun isApplicable(format: String): Boolean {
        return SeriesFourRecord.FORMAT == format
    }
}
