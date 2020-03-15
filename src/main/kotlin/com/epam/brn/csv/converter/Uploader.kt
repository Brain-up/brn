package com.epam.brn.csv.converter

import com.fasterxml.jackson.databind.ObjectReader

interface Uploader<Csv, Entity> : CsvToEntityConverter<Csv, Entity> {
    fun shouldProcess(fileName: String): Boolean
    fun save(entity: Entity)
    fun objectReader(): ObjectReader
}
