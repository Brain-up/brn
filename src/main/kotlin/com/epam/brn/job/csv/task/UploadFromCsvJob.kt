package com.epam.brn.job.csv.task

import com.epam.brn.exception.FileFormatException
import org.springframework.web.multipart.MultipartFile
import java.io.File

interface UploadFromCsvJob {

    /**
     * @param file - csv-file from multipart request which should be convert to model and saved
     *
     * @return failed csv-lines with errors
     */
    @Throws(FileFormatException::class)
    fun uploadTasks(file: MultipartFile): Map<String?, String?>

    /**
     * @param file - csv-file which should be convert to model and saved
     *
     * @return failed csv-lines with errors
     */
    @Throws(FileFormatException::class)
    fun uploadTasks(file: File): Map<String?, String?>
}