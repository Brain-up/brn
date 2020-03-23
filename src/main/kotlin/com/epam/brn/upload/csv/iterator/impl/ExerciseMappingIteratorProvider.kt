package com.epam.brn.upload.csv.iterator.impl

import com.epam.brn.upload.csv.dto.ExerciseCsv
import com.epam.brn.upload.csv.iterator.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.InputStream
import org.apache.commons.lang3.StringUtils

class ExerciseMappingIteratorProvider : MappingIteratorProvider<ExerciseCsv> {

    override fun iterator(file: InputStream): MappingIterator<ExerciseCsv> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(ExerciseCsv::class.java)
            .withColumnSeparator(',')
            .withLineSeparator(StringUtils.SPACE)
            .withColumnReordering(true)
            .withHeader()

        return csvMapper
            .readerWithTypedSchemaFor(ExerciseCsv::class.java)
            .with(csvSchema)
            .readValues(file)
    }
}
