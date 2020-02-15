package com.epam.brn.job.csv.task.impl

import com.epam.brn.constant.BrnErrors.CSV_FILE_FORMAT_ERROR
import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.CsvUtils
import com.epam.brn.job.csv.task.UploadFromCsvService
import java.io.File
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class UploadFromCsvService(
    private val uploadFromCsvFirstSeriesStrategy: UploadFromCsvFirstSeriesStrategy,
    private val uploadFromCsvSecondSeriesStrategy: UploadFromCsvSecondSeriesStrategy
) : UploadFromCsvService {

    @Throws(FileFormatException::class)
    override fun loadTaskFile(file: MultipartFile, seriesId: Long): Map<String, String> {
        if (!isFileContentTypeCsv(file.contentType ?: StringUtils.EMPTY))
            throw FileFormatException(CSV_FILE_FORMAT_ERROR)
        return when (seriesId.toInt()) {
            1 -> uploadFromCsvFirstSeriesStrategy.uploadFile(file.inputStream)
            2 -> uploadFromCsvSecondSeriesStrategy.uploadFile(file.inputStream)
            else -> throw IllegalArgumentException("There no one strategy yet for seriesId = $seriesId")
        }
    }

    @Throws(FileFormatException::class)
    override fun loadTaskFile(file: File): Map<String, String> =
        uploadFromCsvFirstSeriesStrategy.uploadFile(file.inputStream())

    private fun isFileContentTypeCsv(contentType: String): Boolean = CsvUtils.isFileContentTypeCsv(contentType)
}
