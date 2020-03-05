package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.CsvToEntityConverter
import com.epam.brn.csv.converter.InitialDataUploader
import com.epam.brn.csv.converter.ObjectReaderProvider
import java.io.InputStream

class DefaultInitialDataUploader<Csv, Entity>(
    converterTemp: CsvToEntityConverter<Csv, Entity>,
    objectReaderProviderTemp: ObjectReaderProvider<Csv>,
    private val initialDataUploader: InitialDataUploader<Entity>
) : DefaultEntityConverter<Csv, Entity>(converterTemp, objectReaderProviderTemp) {
    fun saveEntitiesInitial(inputStream: InputStream) {
        val entities = this.streamToEntity(inputStream)
        initialDataUploader.saveEntitiesInitialFromMap(entities)
    }
}
