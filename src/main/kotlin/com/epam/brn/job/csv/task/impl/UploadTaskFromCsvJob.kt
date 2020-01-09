package com.epam.brn.job.csv.task.impl

import com.epam.brn.constant.BrnErrors.CSV_FILE_FORMAT_ERROR
import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.CsvUtils
import com.epam.brn.job.csv.task.UploadFromCsvJob
import com.epam.brn.model.Task
import com.epam.brn.service.TaskService
import com.epam.brn.service.parsers.csv.CSVParserService
import com.epam.brn.service.parsers.csv.converter.impl.TaskCsvToTaskModelConverter
import java.io.File
import java.io.InputStream
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class UploadTaskFromCsvJob(private val csvParserService: CSVParserService, private val taskService: TaskService) :
    UploadFromCsvJob {
    private val log = logger()

    @Autowired
    private lateinit var taskCsvToTaskModelConverter: TaskCsvToTaskModelConverter

    @Throws(FileFormatException::class)
    override fun uploadTasks(file: MultipartFile): Map<String, String> {
        if (!isFileContentTypeCsv(file.contentType ?: StringUtils.EMPTY)) throw FileFormatException(CSV_FILE_FORMAT_ERROR)

        return uploadTasks(file.inputStream)
    }

    @Throws(FileFormatException::class)
    override fun uploadTasks(file: File): Map<String, String> {
        return uploadTasks(file.inputStream())
    }

    private fun uploadTasks(inputStream: InputStream): Map<String, String> {
        val tasks = csvParserService.parseCsvFile(inputStream, taskCsvToTaskModelConverter)

        return saveTasks(tasks)
    }

    private fun isFileContentTypeCsv(contentType: String): Boolean {
        return CsvUtils.isFileContentTypeCsv(contentType)
    }

    private fun saveTasks(tasks: Map<String, Pair<Task?, String?>>): Map<String, String> {
        val notSavingTasks = mutableMapOf<String, String>()

        tasks.forEach {
            val key = it.key
            val task = it.value.first

            try {
                if (task != null) {
                    taskService.save(task)
                } else {
                    it.value.second?.let { errorMessage -> notSavingTasks[key] = errorMessage }
                }
            } catch (e: Exception) {
                notSavingTasks[key] = e.localizedMessage
                log.warn("Failed to insert : $key ", e)
            }

            log.debug("Successfully inserted line: $key")
        }

        return notSavingTasks
    }
}
