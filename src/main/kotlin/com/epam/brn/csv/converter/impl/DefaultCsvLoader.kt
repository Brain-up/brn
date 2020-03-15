package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.Uploader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.util.stream.Stream
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.core.io.InputStreamSource
import org.springframework.stereotype.Service

@Service
class DefaultCsvLoader(
    val defaultEntityConverter: DefaultEntityConverter,
    val streamToStringMapper: StreamToStringMapper,
    val uploaders: List<Uploader<out Any, out Any>>
) {
    private val log = logger()

    fun <Csv, Entity> saveEntities(inputStreamSource: InputStreamSource, uploader: Uploader<Csv, Entity>): Map<String, String> {
        return saveEntities(inputStreamSource.inputStream, uploader)
    }

    fun <Csv, Entity> saveEntities(file: File, uploader: Uploader<Csv, Entity>): Map<String, String> {
        return saveEntities(file.inputStream(), uploader)
    }

    private fun <Csv, Entity> saveEntities(inputStream: InputStream, uploader: Uploader<Csv, Entity>): Map<String, String> {
        ByteArrayInputStream(IOUtils.toByteArray(inputStream)).use {
            val rawCsvByLine = streamToStringMapper.getCsvLineNumbersToValues(it)
            val mappingIterator = uploader.objectReader().readValues<Csv>(it)
            val entities = defaultEntityConverter.toEntity<Csv, Entity>(rawCsvByLine, mappingIterator, uploader)
            return save(entities, uploader)
        }
    }

    private fun <Entity> save(
        entities: Stream<DefaultEntityConverter.DataConversionResult<Entity>>,
        uploader: Uploader<*, Entity>
    ): Map<String, String> {
        val notSavingEntities = mutableMapOf<String, String>()

        entities.forEach {
            try {
                if (it.data.isPresent)
                    uploader.save(it.data.get())
                else
                    notSavingEntities[it.line] = it.toString()
            } catch (e: Exception) {
                notSavingEntities[it.line] = "Couldn't save entity $it"
                log.warn("Couldn't save entity $it", e)
            }
            log.debug("Successfully inserted line: $it")
        }
        return notSavingEntities
    }

    fun process(paths: Iterable<Path>) {
        val priorities = mapOf("groups.csv" to 0, "series.csv" to 1, "exercises.csv" to 2)
        val pathsList = paths.toMutableList()
        pathsList.sortBy { priorities[it.fileName.toString()] ?: Integer.MAX_VALUE }
        pathsList.forEach { path ->
            uploaders.stream()
                .filter { it.shouldProcess(path.fileName.toString()) }
                .findFirst()
                .ifPresent { uploader -> saveEntities(path.toFile(), uploader) }
        }
    }
}
