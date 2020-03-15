package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.DataLoadingBeanProvider
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
    val dataLoadingBeanProviders: List<DataLoadingBeanProvider<out Any, out Any>>
) {
    private val log = logger()

    fun <Csv, Entity> saveEntities(inputStreamSource: InputStreamSource, dataLoadingBeanProvider: DataLoadingBeanProvider<Csv, Entity>): Map<String, String> {
        return saveEntities(inputStreamSource.inputStream, dataLoadingBeanProvider)
    }

    fun <Csv, Entity> saveEntities(file: File, dataLoadingBeanProvider: DataLoadingBeanProvider<Csv, Entity>): Map<String, String> {
        return saveEntities(file.inputStream(), dataLoadingBeanProvider)
    }

    private fun <Csv, Entity> saveEntities(inputStream: InputStream, dataLoadingBeanProvider: DataLoadingBeanProvider<Csv, Entity>): Map<String, String> {
        ByteArrayInputStream(IOUtils.toByteArray(inputStream)).use {
            val rawCsvByLine = streamToStringMapper.getCsvLineNumbersToValues(it)
            val mappingIterator = dataLoadingBeanProvider.objectReaderProvider().objectReader<Csv>().readValues<Csv>(it)
            val entities = defaultEntityConverter.toEntity<Csv, Entity>(rawCsvByLine, mappingIterator, dataLoadingBeanProvider.converter())
            return save(entities, dataLoadingBeanProvider)
        }
    }

    private fun <Entity> save(
        entities: Stream<DataConversionResult<Entity>>,
        dataLoadingBeanProvider: DataLoadingBeanProvider<*, Entity>
    ): Map<String, String> {
        val notSavingEntities = mutableMapOf<String, String>()

        entities.forEach {
            try {
                if (it.data.isPresent)
                    dataLoadingBeanProvider.repository().save(it.data.get())
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
            dataLoadingBeanProviders.stream()
                .filter { it.shouldProcess(path.fileName.toString()) }
                .findFirst()
                .ifPresent { uploader -> saveEntities(path.toFile(), uploader) }
        }
    }
}
