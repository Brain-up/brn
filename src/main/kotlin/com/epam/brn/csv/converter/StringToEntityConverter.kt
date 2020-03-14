package com.epam.brn.csv.converter

import com.fasterxml.jackson.databind.MappingIterator

interface StringToEntityConverter {
    fun <Csv, Entity> toEntity(
        rawCsvByLine: Map<Int, String>,
        mappingIterator: MappingIterator<Csv>,
        converter: CsvToEntityConverter<Csv, Entity>
    ): Map<String, Pair<Entity?, String?>>

    fun <Csv> parseCsvFile(
        rawCsvByLine: Map<Int, String>,
        mappingIterator: MappingIterator<Csv>
    ): Map<String, Pair<Csv?, String?>>
}
