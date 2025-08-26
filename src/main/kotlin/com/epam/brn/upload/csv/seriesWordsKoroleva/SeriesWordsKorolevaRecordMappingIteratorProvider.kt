package com.epam.brn.upload.csv.seriesWordsKoroleva

import com.epam.brn.upload.csv.MappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class SeriesWordsKorolevaRecordMappingIteratorProvider : MappingIteratorProvider<SeriesWordsKorolevaRecord> {
    override fun iterator(inputStream: InputStream): MappingIterator<SeriesWordsKorolevaRecord> {
        val csvMapper =
            CsvMapper().apply {
                enable(CsvParser.Feature.TRIM_SPACES)
            }

        val csvSchema =
            csvMapper
                .schemaFor(SeriesWordsKorolevaRecord::class.java)
                .withColumnReordering(true)
                .withArrayElementSeparator(StringUtils.SPACE)
                .withHeader()

        return csvMapper
            .readerFor(SeriesWordsKorolevaRecord::class.java)
            .with(csvSchema)
            .readValues(inputStream)
    }

    override fun isApplicable(format: String): Boolean = SeriesWordsKorolevaRecord.FORMAT == format
}
