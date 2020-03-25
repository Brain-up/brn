package com.epam.brn.upload.csv.parser.iterator.impl

import com.epam.brn.upload.csv.parser.iterator.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.InputStream

class SeriesTwoRecordMappingIteratorProvider : MappingIteratorProvider<Map<String, Any>> {

    override fun iterator(file: InputStream): MappingIterator<Map<String, Any>> {
        val csvMapper = CsvMapper()

        val csvSchema = CsvSchema
            .emptySchema()
            .withHeader()
            .withColumnSeparator(',')
            .withColumnReordering(true)
            .withLineSeparator(",")
            .withArrayElementSeparator(";")

        return csvMapper
            .readerFor(Map::class.java)
            .with(csvSchema)
            .readValues(file)
    }
}
