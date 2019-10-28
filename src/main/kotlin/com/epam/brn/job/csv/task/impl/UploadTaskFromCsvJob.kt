package com.epam.brn.job.csv.task.impl

import com.epam.brn.constant.BrnErrors.CSV_FILE_FORMAT_ERROR
import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.CsvUtils
import com.epam.brn.job.csv.task.UploadFromCsvJob
import com.epam.brn.service.TaskService
import com.epam.brn.service.parsers.csv.CSVParserService
import com.epam.brn.service.parsers.csv.converter.impl.TaskCsvToTaskModelConverter
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.File

import java.io.InputStream

@Component
class UploadTaskFromCsvJob(private val csvParserService: CSVParserService, private val taskService: TaskService) :
    UploadFromCsvJob {

    @Throws(FileFormatException::class)
    override fun uploadTasks(file: MultipartFile) {
        if (!isFileContentTypeCsv(file.contentType ?: StringUtils.EMPTY)) throw FileFormatException(CSV_FILE_FORMAT_ERROR)

        uploadTasks(file.inputStream)
    }

    @Throws(FileFormatException::class)
    override fun uploadTasks(file: File) {
        uploadTasks(file.inputStream())
    }

    @Transactional
    private fun uploadTasks(inputStream: InputStream) {
        val tasks = csvParserService.parseCsvFile(inputStream, TaskCsvToTaskModelConverter())
        tasks.forEach { taskService.save(it) }
    }

    private fun isFileContentTypeCsv(contentType: String): Boolean {
        return CsvUtils.isFileContentTypeCsv(contentType)
    }
}