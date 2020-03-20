package com.epam.brn.csv

import com.epam.brn.csv.converter.Converter
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class CsvMappingIteratorParser {

    val log = logger()

    companion object {
        const val ARRAY_OFFSET = -1
    }

    final inline fun <reified Source, reified Target> parseCsvFile(
        file: InputStream,
        converter: Converter<Source, Target>,
        csvParser: CsvParser<Source>
    ): Map<String, Pair<Target?, String?>> {

        ByteArrayInputStream(IOUtils.toByteArray(file)).use {
            val parsedValues = hashMapOf<String, Source>()
            val sourceToTarget = hashMapOf<String, Pair<Target?, String?>>()

            val originalLines = getOriginalLines(it)

            val parsingIterator = csvParser.parseCsvFile(it)
            while (parsingIterator.hasNextValue()) {
                val lineNumberInFile = parsingIterator.currentLocation.lineNr

                val originalValue = originalLines[lineNumberInFile + ARRAY_OFFSET]
                try {
                    parsedValues[originalValue] = parsingIterator.nextValue()
                    log.debug("Successfully parsed line with number $lineNumberInFile")
                } catch (e: Exception) {
                    sourceToTarget[originalValue] =
                        Pair(null, "Parse Exception - wrong format: ${e.localizedMessage}")

                    log.error("Failed to parse line with number $lineNumberInFile ", e)
                }
            }
            sourceToTarget.putAll(parsedValues
                .map { parsedValue -> parsedValue.key to Pair(converter.convert(parsedValue.value), null) }
                .toMap())

            return sourceToTarget
        }
    }

    fun getOriginalLines(inputStream: InputStream): MutableList<String> {
        val originalLines = BufferedReader(InputStreamReader(inputStream)).lines().collect(Collectors.toList())
        inputStream.reset()
        return originalLines
    }
}
