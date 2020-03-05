package com.epam.brn.csv.converter

import com.epam.brn.csv.CsvMappingIteratorParser
import java.io.InputStream

interface StreamToEntityConverter<Csv, Entity> : ObjectReaderProvider<Csv> {
    companion object {
        val csvMappingIteratorParser = CsvMappingIteratorParser()
    }

    fun convert(source: Csv): Entity
    fun streamToEntity(inputStream: InputStream): Map<String, Pair<Entity?, String?>> {
        val csvMap = csvMappingIteratorParser.parseCsvFile<Csv>(
            inputStream,
            objectReader()
        )
        val entityOrErrors = HashMap<String, Pair<Entity?, String?>>()
        for (csvEntry in csvMap) {
            var entityOrError: Pair<Entity?, String?>
            val csv = csvEntry.value.first
            val error = csvEntry.value.second
            if (csv != null) {
                entityOrError = Pair(convert(csv), null)
            } else {
                entityOrError = Pair(null, error)
            }
            entityOrErrors.put(csvEntry.key, entityOrError)
        }
        return entityOrErrors
    }
}
