package com.epam.brn.upload.csv.parser.iterator.impl

import com.epam.brn.upload.csv.parser.iterator.MappingIteratorProvider
import com.epam.brn.upload.csv.record.SeriesThreeRecord
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.InputStream
import org.apache.commons.lang3.StringUtils.SPACE

class SeriesThreeRecordMappingIteratorProvider : MappingIteratorProvider<SeriesThreeRecord> {

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
}
