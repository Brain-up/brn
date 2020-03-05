package com.epam.brn.csv.converter

import java.io.InputStream

interface StreamToEntityConverter<Entity> {
    fun streamToEntity(inputStream: InputStream): Map<String, Pair<Entity?, String?>>
}
