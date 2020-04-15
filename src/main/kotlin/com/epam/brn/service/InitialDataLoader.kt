package com.epam.brn.service

import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.upload.CsvUploadService
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

/**
 * This class is responsible for
 * loading seed data on startup.
 */
@Service
@Profile("dev", "prod")
class InitialDataLoader(
    private val resourceLoader: ResourceLoader,
    private val exerciseGroupRepository: ExerciseGroupRepository,
    private val uploadService: CsvUploadService
) {
    private val log = logger()

    @Value("\${init.folder:#{null}}")
    var directoryPath: Path? = null

    companion object {
        fun fileNameForSeries(seriesId: Long) = "${seriesId}_series.csv"
        fun getInputStreamFromSeriesInitFile(seriesId: Long): InputStream {
            val inputStream = Thread.currentThread()
                .contextClassLoader.getResourceAsStream("initFiles/${fileNameForSeries(seriesId)}")
                ?: throw IOException("Can not get init file for $seriesId series.")
            return inputStream
        }
    }

    private val sourceFiles = listOf(
        "groups.csv", "series.csv",
        fileNameForSeries(1),
        fileNameForSeries(2),
        fileNameForSeries(3)
    )

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (isInitRequired())
            init()
    }

    private fun isInitRequired() = exerciseGroupRepository.count() == 0L

    private fun init() {
        log.debug("Initialization started")
        if (directoryPath != null)
            initDataFromDirectory(directoryPath!!)
        else
            initDataFromClassPath()
    }

    private fun initDataFromDirectory(directoryToScan: Path) {
        log.debug("Loading data from $directoryToScan.")
        if (!Files.exists(directoryToScan) || !Files.isDirectory(directoryPath))
            throw IllegalArgumentException("$directoryToScan with initial data does not exist")
        sourceFiles.forEach {
            loadFromInputStream(
                Files.newInputStream(directoryToScan.resolve(it))
            )
        }
    }

    private fun initDataFromClassPath() {
        log.debug("Loading data from classpath 'initFiles' directory.")
        sourceFiles.forEach {
            loadFromInputStream(
                resourceLoader.getResource("classpath:initFiles/$it").inputStream
            )
        }
    }

    private fun loadFromInputStream(inputStream: InputStream) {
        try {
            uploadService.load(inputStream)
        } finally {
            closeSilently(inputStream)
        }
    }

    private fun closeSilently(inputStream: InputStream) {
        try {
            inputStream.close()
        } catch (e: Exception) {
            log.error(e)
        }
    }
}
