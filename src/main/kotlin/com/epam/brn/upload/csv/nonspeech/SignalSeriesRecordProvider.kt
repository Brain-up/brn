package com.epam.brn.upload.csv.nonspeech

import com.epam.brn.upload.csv.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class SignalSeriesRecordProvider : MappingIteratorProvider<SignalSeriesRecord> {
    override fun iterator(inputStream: InputStream): MappingIterator<SignalSeriesRecord> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(SignalSeriesRecord::class.java)
            .withColumnReordering(true)
            .withArrayElementSeparator(";")
            .withHeader()

        return csvMapper
            .readerFor(SignalSeriesRecord::class.java)
            .with(csvSchema)
            .readValues(inputStream)
    }

    override fun isApplicable(format: String): Boolean {
        return SignalSeriesRecord.FORMAT == format
    }
}
