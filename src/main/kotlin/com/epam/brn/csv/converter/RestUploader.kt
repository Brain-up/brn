package com.epam.brn.csv.converter

import java.io.InputStream

interface RestUploader<Csv, Entity> : StreamToEntityConverter<Csv, Entity> {
    fun saveEntitiesRest(inputStream: InputStream): Map<String, String> {
        val entities = this.streamToEntity(inputStream)
        return saveEntitiesRestFromMap(entities)
    }
    fun saveEntitiesRestFromMap(entities: Map<String, Pair<Entity?, String?>>): Map<String, String>
}
