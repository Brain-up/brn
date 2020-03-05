package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.CsvToEntityConverter
import com.epam.brn.csv.converter.ObjectReaderProvider
import com.epam.brn.csv.converter.StreamToEntityConverter
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apache.logging.log4j.kotlin.logger

open class DefaultEntityConverter() : StreamToEntityConverter {

    val log = logger()

    override fun <Csv, Entity> streamToEntity(
        inputStream: InputStream,
        converter: CsvToEntityConverter<Csv, Entity>,
        objectReaderProvider: ObjectReaderProvider<Csv>
    ): Map<String, Pair<Entity?, String?>> {
        val csvMap = parseCsvFile(
            inputStream,
            objectReaderProvider
        )
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

    fun <Csv> parseCsvFile(
        file: InputStream,
        objectReaderProvider: ObjectReaderProvider<Csv>
    ): Map<String, Pair<Csv?, String?>> {
        ByteArrayInputStream(IOUtils.toByteArray(file)).use {
            return parseCsvFile(it, objectReaderProvider)
        }
    }

    fun <Csv> parseCsvFile(
        file: ByteArrayInputStream,
        objectReaderProvider: ObjectReaderProvider<Csv>
    ): Map<String, Pair<Csv?, String?>> {
        val csvLineNumbersToValues = getCsvLineNumbersToValues(file)
        val mappingIterator = objectReaderProvider.objectReader().readValues<Csv>(file)
        val parsedValues = hashMapOf<String, Pair<Csv?, String?>>()

        while (mappingIterator.hasNextValue()) {
            val lineNumber = mappingIterator.currentLocation.lineNr
            try {
                val line = mappingIterator.nextValue()
                csvLineNumbersToValues[lineNumber]?.let {
                    parsedValues[it] = Pair(line, null)
                }

                log.debug("Successfully parsed line with number $lineNumber")
            } catch (e: Exception) {
                csvLineNumbersToValues[lineNumber]?.let {
                    parsedValues[it] = Pair(null, "Parse Exception - wrong format: ${e.localizedMessage}")
                }

                log.error("Failed to parse line with number $lineNumber ", e)
            }
        }

        return parsedValues
    }

    fun getCsvLineNumbersToValues(file: InputStream): Map<Int, String> {
        val reader = BufferedReader(InputStreamReader(file))

        val result = mutableMapOf<Int, String>()
        val listOfLinesWithoutHeader = reader
            .lines()
            .skip(NumberUtils.LONG_ONE)
            .collect(Collectors.toList())
        listOfLinesWithoutHeader.forEachIndexed { index, s ->
            result[index + 2] = s
        }

        file.reset()
        return result
    }
}
