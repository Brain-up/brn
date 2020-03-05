package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.Uploader
import java.io.InputStream
import org.springframework.stereotype.Service

@Service
class DefaultInitialDataUploader(val defaultEntityConverter: DefaultEntityConverter) {

    fun <Csv, Entity> saveEntities(inputStream: InputStream, uploader: Uploader<Csv, Entity>) {
        val entities = defaultEntityConverter.streamToEntity(inputStream, uploader, uploader)
        val sorted = mapToList(entities, uploader.entityComparator())
        sorted.forEach { uploader.persistEntity(it!!) }
    }
    fun <Entity> mapToList(entities: Map<String, Pair<Entity?, String?>>, comparator: (Entity) -> Int): Iterable<Entity> {
        val unsorted = entities.map(Map.Entry<String, Pair<Entity?, String?>>::value)
            .map(Pair<Entity?, String?>::first)
            .map { entity -> entity!! }
            .toMutableList()
        return unsorted.sortedBy { comparator.invoke(it) }
    }
}
