package com.epam.brn.upload.csv.parser.iterator.impl

import com.epam.brn.upload.csv.parser.iterator.MappingIteratorProvider
import com.epam.brn.upload.csv.record.SeriesTwoRecord
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.InputStream

class SeriesTwoRecordMappingIteratorProvider : MappingIteratorProvider<SeriesTwoRecord> {

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
}
