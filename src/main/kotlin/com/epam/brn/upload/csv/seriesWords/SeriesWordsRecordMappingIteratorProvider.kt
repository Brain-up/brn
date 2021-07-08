package com.epam.brn.upload.csv.seriesWords

import com.epam.brn.upload.csv.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class SeriesWordsRecordMappingIteratorProvider :
    MappingIteratorProvider<SeriesWordsRecord> {

    override fun iterator(inputStream: InputStream): MappingIterator<SeriesWordsRecord> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(SeriesWordsRecord::class.java)
            .withColumnReordering(true)
            .withArrayElementSeparator(StringUtils.SPACE)
            .withHeader()

        return csvMapper
            .readerFor(SeriesWordsRecord::class.java)
            .with(csvSchema)
            .readValues(inputStream)
    }

    override fun isApplicable(format: String): Boolean {
        return SeriesWordsRecord.FORMAT == format
    }
}
