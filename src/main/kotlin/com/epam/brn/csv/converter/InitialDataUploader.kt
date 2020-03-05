package com.epam.brn.csv.converter

interface InitialDataUploader<Entity> {
    fun saveEntitiesInitialFromMap(entities: Map<String, Pair<Entity?, String?>>)
    fun mapToList(entities: Map<String, Pair<Entity?, String?>>): List<Entity?> {
        return entities.map(Map.Entry<String, Pair<Entity?, String?>>::value)
            .map(Pair<Entity?, String?>::first)
            .toList()
    }
}
