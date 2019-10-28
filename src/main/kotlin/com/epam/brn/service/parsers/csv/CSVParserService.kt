package com.epam.brn.service.parsers.csv

import com.epam.brn.service.parsers.csv.converter.Converter
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class CSVParserService {

    final inline fun <reified Source, reified Target> parseCsvFile(file: InputStream, converter: Converter<Source, Target>): List<Target> {
        val csvMapper = CsvMapper()

        val csvSchema = csvMapper
            .schemaFor(Source::class.java)
            .withArrayElementSeparator(StringUtils.SPACE)
            .withHeader()

        return csvMapper
            .readerWithTypedSchemaFor(Source::class.java)
            .with(csvSchema)
            .readValues<Source>(file)
            .readAll()
            .map(converter::convert)
    }
}