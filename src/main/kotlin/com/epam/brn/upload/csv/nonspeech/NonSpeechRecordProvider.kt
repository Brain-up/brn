package com.epam.brn.upload.csv.nonspeech

import com.epam.brn.upload.csv.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class NonSpeechRecordProvider : MappingIteratorProvider<NonSpeechRecord> {
    override fun iterator(inputStream: InputStream): MappingIterator<NonSpeechRecord> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(NonSpeechRecord::class.java)
            .withColumnReordering(true)
            .withArrayElementSeparator(";")
            .withHeader()

        return csvMapper
            .readerFor(NonSpeechRecord::class.java)
            .with(csvSchema)
            .readValues(inputStream)
    }

    override fun isApplicable(format: String): Boolean {
        return NonSpeechRecord.FORMAT == format
    }
}
