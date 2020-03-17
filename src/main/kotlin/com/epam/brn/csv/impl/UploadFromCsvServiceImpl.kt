package com.epam.brn.csv.impl

import com.epam.brn.constant.BrnErrors.CSV_FILE_FORMAT_ERROR
import com.epam.brn.csv.UploadFromCsvService
import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.CsvUtils
import java.io.File
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class UploadFromCsvServiceImpl(
    private val uploadFromCsv1SeriesStrategy: UploadFromCsv1SeriesStrategy,
    private val uploadFromCsv2SeriesStrategy: UploadFromCsv2SeriesStrategy
) : UploadFromCsvService {

    @Throws(FileFormatException::class)
    override fun loadTaskFile(file: MultipartFile, seriesId: Long): Map<String, String> {

        if (!isFileContentTypeCsv(file.contentType ?: StringUtils.EMPTY))
            throw FileFormatException(CSV_FILE_FORMAT_ERROR)

        return when (seriesId.toInt()) {
            1 -> uploadFromCsv1SeriesStrategy.uploadFile(file.inputStream)
            2 -> uploadFromCsv2SeriesStrategy.uploadFile(file.inputStream)
            else -> throw IllegalArgumentException("There no one strategy yet for seriesId = $seriesId")
        }
    }

    @Throws(FileFormatException::class)
    override fun loadTaskFile(file: File): Map<String, String> =
        uploadFromCsv1SeriesStrategy.uploadFile(file.inputStream())

    private fun isFileContentTypeCsv(contentType: String): Boolean = CsvUtils.isFileContentTypeCsv(contentType)
}
