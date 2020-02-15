package com.epam.brn.job.csv.task

import java.io.InputStream

interface UploadFromCsvStrategy {

    fun uploadFile(inputStream: InputStream): Map<String, String>
}
