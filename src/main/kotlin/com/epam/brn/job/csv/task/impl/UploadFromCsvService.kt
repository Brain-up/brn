package com.epam.brn.job.csv.task.impl

import com.epam.brn.constant.BrnErrors.CSV_FILE_FORMAT_ERROR
import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.CsvUtils
import com.epam.brn.job.csv.task.UploadFromCsvService
import java.io.File
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class UploadFromCsvService : UploadFromCsvService {

    @Autowired
    private lateinit var uploadFromCsvServiceFirstSeriesStrategy: UploadFromCsvServiceFirstSeriesStrategy

    @Autowired
    private lateinit var uploadFromCsvServiceSecondSeriesStrategy: UploadFromCsvServiceSecondSeriesStrategy

    @Throws(FileFormatException::class)
    override fun loadTaskFile(file: MultipartFile, seriesId: Long?): Map<String, String> {
        if (!isFileContentTypeCsv(file.contentType ?: StringUtils.EMPTY))
            throw FileFormatException(CSV_FILE_FORMAT_ERROR)

        return if (seriesId!! == 1.toLong())
            uploadFromCsvServiceFirstSeriesStrategy.uploadFile(file.inputStream)
        else
            uploadFromCsvServiceSecondSeriesStrategy.uploadFile(file.inputStream)
    }

    @Throws(FileFormatException::class)
    override fun loadTaskFile(file: File): Map<String, String> = uploadFromCsvServiceFirstSeriesStrategy.uploadFile(file.inputStream())

    private fun isFileContentTypeCsv(contentType: String): Boolean = CsvUtils.isFileContentTypeCsv(contentType)
}
