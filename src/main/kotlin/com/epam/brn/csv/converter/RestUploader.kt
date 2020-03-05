package com.epam.brn.csv.converter

interface RestUploader<Entity> {
    fun saveEntitiesRestFromMap(entities: Map<String, Pair<Entity?, String?>>): Map<String, String>
}
