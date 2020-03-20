package com.epam.brn.csv

import java.io.InputStream

interface UploadFromCsvStrategy {

    fun uploadFile(inputStream: InputStream): List<Any>
}
