package com.epam.brn.service.parsers.csv

import com.epam.brn.service.parsers.csv.converter.Converter
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.springframework.stereotype.Service
import java.io.InputStream
import java.util.Optional

@Service
class CSVParserService {

    final inline fun <reified Source, reified Target> parseCsvFile(file: InputStream, converter: Converter<Source, Target>): List<Target> {
        val csvSchema = CsvMapper().schemaFor(Source::class.java)
            .withArrayElementSeparator(" ")
            .withHeader()

        val taskIterator = CsvMapper()
            .readerWithTypedSchemaFor(Source::class.java)
            .with(csvSchema)
            .readValues<Source>(file)

        return Optional.ofNullable(taskIterator)
            .map(MappingIterator<Source>::readAll)
            .orElse(emptyList())
            .map(converter::convert)
    }
}