package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.Uploader
import java.io.InputStream

class DefaultInitialDataUploader<Csv, Entity>(
    private val uploader: Uploader<Csv, Entity>
) {
    private val defaultEntityConverter = DefaultEntityConverter(uploader, uploader)
    fun saveEntities(inputStream: InputStream) {
        val entities = defaultEntityConverter.streamToEntity(inputStream)
        val sorted = mapToList(entities, uploader.entityComparator())
        sorted.forEach { uploader.persistEntity(it!!) }
    }
    fun mapToList(entities: Map<String, Pair<Entity?, String?>>, comparator: (Entity) -> Int): Iterable<Entity> {
        val unsorted = entities.map(Map.Entry<String, Pair<Entity?, String?>>::value)
            .map(Pair<Entity?, String?>::first)
            .map { entity -> entity!! }
            .toMutableList()
        return unsorted.sortedBy { comparator.invoke(it) }
    }
}
