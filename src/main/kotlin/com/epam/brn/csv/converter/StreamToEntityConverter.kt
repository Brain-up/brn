package com.epam.brn.csv.converter

import java.io.InputStream

interface StreamToEntityConverter {
    fun <Csv, Entity> streamToEntity(inputStream: InputStream, converter: CsvToEntityConverter<Csv, Entity>, objectReaderProvider: ObjectReaderProvider<Csv>): Map<String, Pair<Entity?, String?>>
}
