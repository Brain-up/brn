package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.Uploader
import java.io.InputStream
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class DefaultRestUploader(val defaultEntityConverter: DefaultEntityConverter) {
    private val log = logger()

    fun <Csv, Entity> saveEntities(inputStream: InputStream, uploader: Uploader<Csv, Entity>): Map<String, String> {
        val entities = defaultEntityConverter.streamToEntity(inputStream, uploader, uploader)
        return save(entities, uploader)
    }

    private fun <Entity> save(entities: Map<String, Pair<Entity?, String?>>, uploader: Uploader<*, Entity>): Map<String, String> {
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
