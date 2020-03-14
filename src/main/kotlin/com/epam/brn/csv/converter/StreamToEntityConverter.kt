package com.epam.brn.csv.converter

import java.io.InputStream

interface StreamToEntityConverter {
    fun <Csv, Entity> streamToEntity(inputStream: InputStream, uploader: Uploader<Csv,Entity>): Map<String, Pair<Entity?, String?>>
    fun <Csv, Entity> parseCsvFile(file: InputStream,uploader: Uploader<Csv,Entity>): Map<String, Pair<Csv?, String?>>
}
