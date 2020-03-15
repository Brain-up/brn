package com.epam.brn.csv.converter

import com.epam.brn.csv.converter.impl.DefaultEntityConverter
import com.fasterxml.jackson.databind.MappingIterator
import java.util.stream.Stream

interface StringToEntityConverter {

    fun <Csv, Entity> toEntity(
        rawCsvByLine: Stream<String>,
        mappingIterator: MappingIterator<Csv>,
        converter: CsvToEntityConverter<Csv, Entity>
    ): Stream<DefaultEntityConverter.DataConversionResult<Entity>>

    fun <Csv> parseCsvFile(
        rawCsvByLine: Stream<String>,
        mappingIterator: MappingIterator<Csv>
    ): Stream<DefaultEntityConverter.DataConversionResult<Csv>>
}
