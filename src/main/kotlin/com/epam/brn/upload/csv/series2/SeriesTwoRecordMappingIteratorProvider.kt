package com.epam.brn.upload.csv.series2

import com.epam.brn.upload.csv.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.InputStream
import org.springframework.stereotype.Component

@Component
class SeriesTwoRecordMappingIteratorProvider :
    MappingIteratorProvider<SeriesTwoRecord> {

    override fun iterator(inputStream: InputStream): MappingIterator<SeriesTwoRecord> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(SeriesTwoRecord::class.java)
            .withColumnReordering(true)
            .withHeader()

        return csvMapper
            .readerFor(SeriesTwoRecord::class.java)
            .with(csvSchema)
            .readValues(inputStream)
    }

    override fun isApplicable(format: String): Boolean {
        return SeriesTwoRecord.FORMAT == format
    }
}
