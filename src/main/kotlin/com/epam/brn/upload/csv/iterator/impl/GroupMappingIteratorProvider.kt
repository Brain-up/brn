package com.epam.brn.upload.csv.iterator.impl

import com.epam.brn.upload.csv.iterator.MappingIteratorProvider
import com.epam.brn.upload.csv.record.GroupCsv
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.InputStream
import org.apache.commons.lang3.StringUtils

class GroupMappingIteratorProvider : MappingIteratorProvider<GroupCsv> {

    override fun iterator(file: InputStream): MappingIterator<GroupCsv> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(GroupCsv::class.java)
            .withColumnSeparator(',')
            .withLineSeparator(StringUtils.SPACE)
            .withColumnReordering(true)
            .withHeader()

        return csvMapper
            .readerWithTypedSchemaFor(GroupCsv::class.java)
            .with(csvSchema)
            .readValues(file)
    }
}
