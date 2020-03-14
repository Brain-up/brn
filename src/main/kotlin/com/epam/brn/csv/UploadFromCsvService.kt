package com.epam.brn.csv

import com.epam.brn.exception.FileFormatException
import java.io.File
import org.springframework.web.multipart.MultipartFile

interface UploadFromCsvService {

    /**
     * @param file - csv task file with from multipart request which should be convert to task model and saved with series
     * @param seriesId - series id
     *
     * @return failed csv-lines with errors
     */
    @Throws(FileFormatException::class)
    fun loadTaskFile(file: MultipartFile, seriesId: Long): Map<String, String>

    /**
     * @param file - csv task file with exercise and orderNumber which should be convert to model and saved
     *
     * @return failed csv-lines with errors
     */
    @Throws(FileFormatException::class)
    fun loadTaskFile(file: File): Map<String, String>
}
