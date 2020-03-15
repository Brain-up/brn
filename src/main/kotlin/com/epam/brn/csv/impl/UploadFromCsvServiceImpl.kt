package com.epam.brn.csv.impl

import com.epam.brn.constant.BrnErrors.CSV_FILE_FORMAT_ERROR
import com.epam.brn.csv.UploadFromCsvService
import com.epam.brn.csv.converter.impl.DefaultCsvLoader
import com.epam.brn.csv.converter.impl.firstSeries.SeriesOneBeanProvider
import com.epam.brn.csv.converter.impl.secondSeries.SeriesTwoBeanProvider
import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.CsvUtils
import java.io.File
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class UploadFromCsvServiceImpl(
    val seriesOneUploader: SeriesOneBeanProvider,
    val seriesTwoUploader: SeriesTwoBeanProvider,
    val defaultCsvLoader: DefaultCsvLoader
) : UploadFromCsvService {

    @Throws(FileFormatException::class)
    override fun loadTaskFile(file: MultipartFile, seriesId: Long): Map<String, String> {
        if (!isFileContentTypeCsv(file.contentType ?: StringUtils.EMPTY))
            throw FileFormatException(CSV_FILE_FORMAT_ERROR)
        return when (seriesId.toInt()) {
            1 -> defaultCsvLoader.saveEntities(file, seriesOneUploader)
            2 -> defaultCsvLoader.saveEntities(file, seriesTwoUploader)
            else -> throw IllegalArgumentException("There no one strategy yet for seriesId = $seriesId")
        }
    }

    @Throws(FileFormatException::class)
    override fun loadTaskFile(file: File) =
        defaultCsvLoader.saveEntities(file, seriesOneUploader)

    private fun isFileContentTypeCsv(contentType: String): Boolean = CsvUtils.isFileContentTypeCsv(contentType)
}
