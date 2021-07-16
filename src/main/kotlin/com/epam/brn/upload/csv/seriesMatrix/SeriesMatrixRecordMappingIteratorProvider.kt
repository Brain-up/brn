package com.epam.brn.upload.csv.seriesMatrix

import com.epam.brn.upload.csv.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class SeriesMatrixRecordMappingIteratorProvider :
    MappingIteratorProvider<SeriesMatrixRecord> {

    override fun iterator(inputStream: InputStream): MappingIterator<SeriesMatrixRecord> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(SeriesMatrixRecord::class.java)
            .withColumnReordering(true)
            .withHeader()

        return csvMapper
            .readerFor(SeriesMatrixRecord::class.java)
            .with(csvSchema)
            .readValues(inputStream)
    }

    override fun isApplicable(format: String): Boolean {
        return SeriesMatrixRecord.FORMAT == format
    }
}
