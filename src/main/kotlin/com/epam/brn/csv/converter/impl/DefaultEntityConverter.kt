package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.CsvToEntityConverter
import com.epam.brn.csv.converter.StringToEntityConverter
import com.fasterxml.jackson.databind.MappingIterator
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class DefaultEntityConverter : StringToEntityConverter {

    val log = logger()

    override fun <Csv, Entity> toEntity(
        rawCsvByLine: Map<Int, String>,
        mappingIterator: MappingIterator<Csv>,
        converter: CsvToEntityConverter<Csv, Entity>
    ): Map<String, Pair<Entity?, String?>> {
        val csvMap = parseCsvFile(rawCsvByLine, mappingIterator)
        return extractEntityFromCsv(csvMap, converter)
    }

    private fun <Csv, Entity> extractEntityFromCsv(
        csvMap: Map<String, Pair<Csv?, String?>>,
        converter: CsvToEntityConverter<Csv, Entity>
    ): HashMap<String, Pair<Entity?, String?>> {
        val entityOrErrors = HashMap<String, Pair<Entity?, String?>>()
        for (csvEntry in csvMap) {
            var entityOrError: Pair<Entity?, String?>
            val csv = csvEntry.value.first
            val error = csvEntry.value.second
            if (csv != null) {
                entityOrError = Pair(converter.convert(csv), null)
            } else {
                entityOrError = Pair(null, error)
            }
            entityOrErrors.put(csvEntry.key, entityOrError)
        }
        return entityOrErrors
    }

    override fun <Csv> parseCsvFile(
        rawCsvByLine: Map<Int, String>,
        mappingIterator: MappingIterator<Csv>
    ): Map<String, Pair<Csv?, String?>> {
        val parsedValues = hashMapOf<String, Pair<Csv?, String?>>()

        while (mappingIterator.hasNextValue()) {
            val lineNumber = mappingIterator.currentLocation.lineNr
            rawCsvByLine[lineNumber]?.let {
                parsedValues[it] = parseNextCsvValue(mappingIterator, lineNumber)
            }
        }

        return parsedValues
    }

    fun <Csv> parseNextCsvValue(iterator: MappingIterator<Csv>, lineNumber: Int): Pair<Csv?, String?> {
        try {
            val line = iterator.nextValue()
            log.debug("Successfully parsed line with number $lineNumber")
            return Pair(line, null)
        } catch (e: Exception) {
            log.error("Failed to parse line with number $lineNumber ", e)
            return Pair(null, "Parse Exception - wrong format: ${e.localizedMessage}")
        }
    }
}
