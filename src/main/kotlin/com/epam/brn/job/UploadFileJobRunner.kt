package com.epam.brn.job

import com.epam.brn.constant.BrnJob.PATH_TO_PRECESSED_RESOURCES
import com.epam.brn.job.csv.task.UploadFromCsvJob
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
class UploadFileJobRunner {

    private val log = logger()

    @Autowired
    private lateinit var sourcesWithJobs: LinkedHashMap<String, UploadFromCsvJob>

    @Scheduled(cron = "\${cron.expression.input.data.upload}")
    fun perform() {
        val jobId = System.currentTimeMillis().toString()
        val successfullyProcessedResources = HashSet<File>()

        sourcesWithJobs.forEach { (path, job) ->
            Files.walk(Paths.get(path))
                .filter(this::isCsvFile)
                .map { filePath -> loadFile(job, filePath, successfullyProcessedResources) }
        }

        moveSuccessfullyProcessedResources(successfullyProcessedResources, jobId)
    }

    private fun loadFile(job: UploadFromCsvJob, filePath: Path, successfullyProcessedResources: HashSet<File>) {
        val file = filePath.toFile()

        try {
            job.uploadTask(file)
            successfullyProcessedResources.add(file)
        } catch (e: Exception) {
            log.error("Something went wrong while loading file ${file.name}", e)
        }
    }

    private fun moveSuccessfullyProcessedResources(successfullyProcessedResources: Set<File>, jobId: String) {
        successfullyProcessedResources.stream()
            .forEach { file ->
                val oldPath = file.path
                val newPath = PATH_TO_PRECESSED_RESOURCES + jobId + "_" + file.name

                FileUtils.moveFile(
                    FileUtils.getFile(oldPath),
                    FileUtils.getFile(newPath)
                )

                log.info("File $oldPath was successfully processed and moved to $newPath")
            }
    }

    private fun isCsvFile(filePath: Path): Boolean {
        return Files.isRegularFile(filePath) && "text/csv".equals(Files.probeContentType(filePath))
    }
}