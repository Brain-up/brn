package com.epam.brn.csv.secondSeries

import com.epam.brn.csv.CsvParser
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.InputStream

class CSVParser2SeriesService : CsvParser<Map<String, Any>> {

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
