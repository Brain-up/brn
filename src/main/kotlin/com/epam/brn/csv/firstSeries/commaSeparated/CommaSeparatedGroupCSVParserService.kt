package com.epam.brn.csv.firstSeries.commaSeparated

import com.epam.brn.csv.dto.GroupCsv
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.InputStream
import org.apache.commons.lang3.StringUtils

class CommaSeparatedGroupCSVParserService : com.epam.brn.csv.CsvParser<GroupCsv> {

    override fun parseCsvFile(file: InputStream): MappingIterator<GroupCsv> {
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
