package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.Uploader
import java.io.ByteArrayInputStream
import java.io.InputStream
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Service

@Service
class DefaultInitialDataUploader(
    val defaultEntityConverter: DefaultEntityConverter,
    val streamToStringMapper: StreamToStringMapper
) {

    fun <Csv, Entity> saveEntities(inputStream: InputStream, uploader: Uploader<Csv, Entity>) {
        ByteArrayInputStream(IOUtils.toByteArray(inputStream)).use {
            val rawCsvByLine = streamToStringMapper.getCsvLineNumbersToValues(it)
            val mappingIterator = uploader.objectReader().readValues<Csv>(it)
            val entities = defaultEntityConverter.toEntity<Csv, Entity>(rawCsvByLine, mappingIterator, uploader)
            val sorted = mapToList(entities, uploader.entityComparator())
            sorted.forEach { entity -> uploader.persistEntity(entity!!) }
        }
    }

    fun <Entity> mapToList(
        entities: Map<String, Pair<Entity?, String?>>,
        comparator: (Entity) -> Int
    ): Iterable<Entity> {
        val unsorted = entities.map(Map.Entry<String, Pair<Entity?, String?>>::value)
            .map(Pair<Entity?, String?>::first)
            .map { entity -> entity!! }
            .toMutableList()
        return unsorted.sortedBy { comparator.invoke(it) }
    }
}
