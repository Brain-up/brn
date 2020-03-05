package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.CsvToEntityConverter
import com.epam.brn.csv.converter.ObjectReaderProvider
import com.epam.brn.csv.converter.RestUploader
import java.io.InputStream

class DefaultRestUploader<Csv, Entity>(
    converterTemp: CsvToEntityConverter<Csv, Entity>,
    objectReaderProviderTemp: ObjectReaderProvider<Csv>,
    private val restUploader: RestUploader<Entity>
) {
    private val defaultEntityConverter = DefaultEntityConverter(converterTemp, objectReaderProviderTemp)
    fun saveEntitiesRest(inputStream: InputStream): Map<String, String> {
        val entities = defaultEntityConverter.streamToEntity(inputStream)
        return restUploader.saveEntitiesRestFromMap(entities)
    }
}
