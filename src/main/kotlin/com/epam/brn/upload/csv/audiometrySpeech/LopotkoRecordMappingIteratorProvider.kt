package com.epam.brn.upload.csv.series1

import com.epam.brn.upload.csv.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class LopotkoRecordMappingIteratorProvider :
    MappingIteratorProvider<LopotkoRecord> {

    override fun iterator(inputStream: InputStream): MappingIterator<LopotkoRecord> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(LopotkoRecord::class.java)
            .withColumnReordering(true)
            .withArrayElementSeparator(StringUtils.SPACE)
            .withHeader()

        return csvMapper
            .readerFor(LopotkoRecord::class.java)
            .with(csvSchema)
            .readValues(inputStream)
    }

    override fun isApplicable(format: String): Boolean {
        return LopotkoRecord.FORMAT == format
    }
}
