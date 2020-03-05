package com.epam.brn.csv.impl

import com.epam.brn.constant.BrnErrors.CSV_FILE_FORMAT_ERROR
import com.epam.brn.csv.UploadFromCsvService
import com.epam.brn.csv.converter.Uploader
import com.epam.brn.csv.converter.impl.DefaultRestUploader
import com.epam.brn.csv.converter.impl.firstSeries.SeriesOneUploader
import com.epam.brn.csv.converter.impl.secondSeries.SeriesTwoUploader
import com.epam.brn.csv.dto.TaskCsv
import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.CsvUtils
import com.epam.brn.model.Exercise
import com.epam.brn.model.Task
import java.io.File
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class UploadFromCsvServiceImpl(
    seriesOneUploader: SeriesOneUploader,
    seriesTwoUploader: SeriesTwoUploader
) : UploadFromCsvService {

    private final val defaultRestUploaderSeriesOne: DefaultRestUploader<TaskCsv, Task>
    private final val defaultRestUploaderSeriesTwo: DefaultRestUploader<Map<String, Any>, Exercise>
    init {
        defaultRestUploaderSeriesOne = getRestUploader(seriesOneUploader)
        defaultRestUploaderSeriesTwo = getRestUploader(seriesTwoUploader)
    }

    private fun <Csv, Entity> getRestUploader(uploader: Uploader<Csv, Entity>): DefaultRestUploader<Csv, Entity> {
        return DefaultRestUploader(uploader)
    }

    @Throws(FileFormatException::class)
    override fun loadTaskFile(file: MultipartFile, seriesId: Long): Map<String, String> {
        if (!isFileContentTypeCsv(file.contentType ?: StringUtils.EMPTY))
            throw FileFormatException(CSV_FILE_FORMAT_ERROR)
        return when (seriesId.toInt()) {
            1 -> defaultRestUploaderSeriesOne.saveEntities(file.inputStream)
            2 -> defaultRestUploaderSeriesTwo.saveEntities(file.inputStream)
            else -> throw IllegalArgumentException("There no one strategy yet for seriesId = $seriesId")
        }
    }

    @Throws(FileFormatException::class)
    override fun loadTaskFile(file: File): Map<String, String>? =
        defaultRestUploaderSeriesOne.saveEntities(file.inputStream())

    private fun isFileContentTypeCsv(contentType: String): Boolean = CsvUtils.isFileContentTypeCsv(contentType)
}
