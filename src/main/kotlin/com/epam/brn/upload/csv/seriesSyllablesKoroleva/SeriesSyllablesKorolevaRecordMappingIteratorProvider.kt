package com.epam.brn.upload.csv.seriesSyllablesKoroleva

import com.epam.brn.upload.csv.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class SeriesSyllablesKorolevaRecordMappingIteratorProvider :
    MappingIteratorProvider<SeriesSyllablesKorolevaRecord> {

    override fun iterator(inputStream: InputStream): MappingIterator<SeriesSyllablesKorolevaRecord> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(SeriesSyllablesKorolevaRecord::class.java)
            .withColumnReordering(true)
            .withArrayElementSeparator(StringUtils.SPACE)
            .withHeader()

        return csvMapper
            .readerFor(SeriesSyllablesKorolevaRecord::class.java)
            .with(csvSchema)
            .readValues(inputStream)
    }

    override fun isApplicable(format: String): Boolean =
        SeriesSyllablesKorolevaRecord.FORMAT == format
}
