package com.epam.brn.job.impl

import com.epam.brn.job.UploadFileJobRunner
import com.epam.brn.upload.CsvUploadService
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("default", "dev")
class UploadFileJobRunnerImpl : UploadFileJobRunner {

    private val log = logger()

    @Value(value = "\${brn.processed.files.path}")
    private lateinit var pathToProcessedResources: String

    @Autowired
    private lateinit var sourcesWithJobs: LinkedHashMap<String, CsvUploadService>

    @Scheduled(cron = "\${cron.expression.input.data.upload}")
    override fun perform() {
        val jobId = System.currentTimeMillis().toString()
        val successfullyProcessedResources = HashSet<File>()

        sourcesWithJobs.forEach { (path, job) ->
            Files.walk(Paths.get(path))
                .filter { isCsvFile(it) }
                .forEach { loadFile(job, it, successfullyProcessedResources) }
        }

        moveSuccessfullyProcessedResources(successfullyProcessedResources, jobId)
    }

    private fun loadFile(
        uploadService: CsvUploadService,
        filePath: Path,
        successfullyProcessedResources: HashSet<File>
    ) {
        val file = filePath.toFile()

        try {
            uploadService.load(file)
            successfullyProcessedResources.add(file)
        } catch (e: Exception) {
            log.error("Something went wrong while loading file ${file.name}", e)
        }
    }

    private fun moveSuccessfullyProcessedResources(successfullyProcessedResources: Set<File>, jobId: String) {
        successfullyProcessedResources.stream()
            .forEach {
                val oldPath = it.path
                val newPath = pathToProcessedResources + File.separator + jobId + "_" + it.name

                FileUtils.moveFile(
                    FileUtils.getFile(oldPath),
                    FileUtils.getFile(newPath)
                )

                log.info("File $oldPath was successfully processed and moved to $newPath")
            }
    }

    private fun isCsvFile(filePath: Path): Boolean {
        return Files.isRegularFile(filePath) &&
                CsvUploadService.isCsvContentType(Files.probeContentType(filePath))
    }
}
