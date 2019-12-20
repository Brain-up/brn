package com.epam.brn.job.csv.task

import com.epam.brn.exception.FileFormatException
import org.springframework.web.multipart.MultipartFile
import java.io.File

interface UploadFromCsvJob {

    /**
     * @param file - csv task file with exercise and orderNumber from multipart request which should be convert to task model and saved
     *
     * @return failed csv-lines with errors
     */
    @Throws(FileFormatException::class)
    fun loadFullTaskFile(file: MultipartFile): Map<String, String>

    /**
     * @param file - csv task file with exercise and orderNumber which should be convert to model and saved
     *
     * @return failed csv-lines with errors
     */
    @Throws(FileFormatException::class)
    fun loadFullTaskFile(file: File): Map<String, String>

    /**
     * @param file - csv-file from multipart request which should be convert to task model and saved
     * @param exerciseId - exercise id
     * @param serialNumber - order number
     *
     * @return failed csv-lines with errors
     */
    @Throws(FileFormatException::class)
    fun loadTaskFile(file: MultipartFile, exerciseId: Long, serialNumber: Int): Map<String, String>
}