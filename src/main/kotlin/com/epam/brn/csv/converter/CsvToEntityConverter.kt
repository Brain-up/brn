package com.epam.brn.csv.converter

interface CsvToEntityConverter<Csv, Entity> {
    fun convert(source: Csv): Entity
}
