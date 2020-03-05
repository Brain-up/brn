package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.Uploader
import java.io.InputStream
import org.apache.logging.log4j.kotlin.logger

class DefaultRestUploader<Csv, Entity>(
    private val uploader: Uploader<Csv, Entity>
) {
    private val log = logger()

    private val defaultEntityConverter = DefaultEntityConverter(uploader, uploader)
    fun saveEntities(inputStream: InputStream): Map<String, String> {
        val entities = defaultEntityConverter.streamToEntity(inputStream)
        return save(entities)
    }

    private fun save(entities: Map<String, Pair<Entity?, String?>>): Map<String, String> {
        val notSavingEntities = mutableMapOf<String, String>()

        entities.forEach {
            val key = it.key
            val task = it.value.first
            try {
                if (task != null)
                    uploader.persistEntity(task)
                else
                    it.value.second?.let { errorMessage -> notSavingEntities[key] = errorMessage }
            } catch (e: Exception) {
                notSavingEntities[key] = e.localizedMessage
                log.warn("Failed to insert : $key ", e)
            }
            log.debug("Successfully inserted line: $key")
        }
        return notSavingEntities
    }
}
