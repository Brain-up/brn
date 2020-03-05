package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.CsvToEntityConverter
import com.epam.brn.csv.converter.ObjectReaderProvider
import com.epam.brn.csv.converter.RestUploader
import java.io.InputStream

class DefaultRestUploader<Csv, Entity>(
    converterTemp: CsvToEntityConverter<Csv, Entity>,
    objectReaderProviderTemp: ObjectReaderProvider<Csv>,
    val restUploader: RestUploader<Entity>
) : DefaultEntityConverter<Csv, Entity>(converterTemp, objectReaderProviderTemp) {
    fun saveEntitiesRest(inputStream: InputStream): Map<String, String> {
        val entities = this.streamToEntity(inputStream)
        return restUploader.saveEntitiesRestFromMap(entities)
    }
}
