package com.epam.brn.upload.csv.series3

import com.epam.brn.upload.csv.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import org.apache.commons.lang3.StringUtils.SPACE
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class SeriesThreeRecordMappingIteratorProvider :
    MappingIteratorProvider<SeriesThreeRecord> {

    override fun iterator(inputStream: InputStream): MappingIterator<SeriesThreeRecord> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(SeriesThreeRecord::class.java)
            .withLineSeparator(SPACE)
            .withColumnReordering(true)
            .withHeader()

        return csvMapper
            .readerWithTypedSchemaFor(SeriesThreeRecord::class.java)
            .with(csvSchema)
            .readValues(inputStream)
    }

    override fun isApplicable(format: String): Boolean {
        return SeriesThreeRecord.FORMAT == format
    }
}
