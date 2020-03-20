package com.epam.brn.csv

import com.epam.brn.csv.impl.UploadFromCsv1SeriesStrategy
import com.epam.brn.csv.impl.UploadFromCsv2SeriesStrategy
import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.CsvUtils
import java.io.File
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class CsvUploadService(
    private val uploadFromCsv1SeriesStrategy: UploadFromCsv1SeriesStrategy,
    private val uploadFromCsv2SeriesStrategy: UploadFromCsv2SeriesStrategy
) {

    @Throws(FileFormatException::class)
    fun loadExercises(seriesId: Long, file: MultipartFile): List<Any> {

        if (!isFileContentTypeCsv(file.contentType ?: StringUtils.EMPTY))
            throw FileFormatException()

        return when (seriesId.toInt()) {
            1 -> uploadFromCsv1SeriesStrategy.uploadFile(file.inputStream)
            2 -> uploadFromCsv2SeriesStrategy.uploadFile(file.inputStream)
            else -> throw IllegalArgumentException("There no one strategy yet for seriesId = $seriesId")
        }
    }

    @Throws(FileFormatException::class)
    fun loadTasks(file: File): List<Any> =
        uploadFromCsv1SeriesStrategy.uploadFile(file.inputStream())

    private fun isFileContentTypeCsv(contentType: String): Boolean = CsvUtils.isFileContentTypeCsv(contentType)
}
