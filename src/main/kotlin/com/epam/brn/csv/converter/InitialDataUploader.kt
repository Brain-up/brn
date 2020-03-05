package com.epam.brn.csv.converter

import java.io.InputStream

interface InitialDataUploader<Csv, Entity> : StreamToEntityConverter<Csv, Entity> {
    fun saveEntitiesInitialFromMap(entities: Map<String, Pair<Entity?, String?>>)
    fun saveEntitiesInitial(inputStream: InputStream) {
        val entities = this.streamToEntity(inputStream)
        saveEntitiesInitialFromMap(entities)
    }
    fun mapToList(entities: Map<String, Pair<Entity?, String?>>): List<Entity?> {
        return entities.map(Map.Entry<String, Pair<Entity?, String?>>::value)
            .map(Pair<Entity?, String?>::first)
            .toList()
    }
}
