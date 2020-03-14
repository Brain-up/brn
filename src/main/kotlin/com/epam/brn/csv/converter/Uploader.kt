package com.epam.brn.csv.converter

import com.fasterxml.jackson.databind.ObjectReader

interface Uploader<Csv, Entity> : CsvToEntityConverter<Csv, Entity>{
    fun entityComparator(): (Entity) -> Int
    fun persistEntity(entity: Entity)
    fun objectReader(): ObjectReader
}
