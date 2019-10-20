package com.epam.brn.job.csv.task.impl

import com.epam.brn.constant.BrnErrors.CSV_FILE_FORMAT_ERROR
import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.csv.task.UploadFromCsvJob
import com.epam.brn.service.TaskService
import com.epam.brn.service.parsers.csv.CSVParserService
import com.epam.brn.service.parsers.csv.converter.impl.TaskCsvToTaskModelConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.File

import java.io.InputStream
import java.nio.file.Files

@Component
class UploadTaskFromCsvJob(@Autowired val csvParserService: CSVParserService, @Autowired val taskService: TaskService) :
    UploadFromCsvJob {

    @Throws(FileFormatException::class)
    override fun uploadTask(file: MultipartFile) {
        if (!isFileContentTypeCsv(file.contentType ?: "")) throw FileFormatException(CSV_FILE_FORMAT_ERROR)

        uploadTask(file.inputStream)
    }

    @Throws(FileFormatException::class)
    override fun uploadTask(file: File) {
        if (!isFileCsv(file)) throw FileFormatException(CSV_FILE_FORMAT_ERROR)

        uploadTask(file.inputStream())
    }

    @Transactional
    private fun uploadTask(inputStream: InputStream) {
        val tasks = csvParserService.parseCsvFile(inputStream, TaskCsvToTaskModelConverter())
        tasks.forEach(taskService::save)
    }

    private fun isFileCsv(file: File): Boolean {
        val contentType = Files.probeContentType(file.toPath())
        return isFileContentTypeCsv(contentType)
    }

    private fun isFileContentTypeCsv(contentType: String): Boolean {
        return "text/csv".equals(contentType)
    }
}