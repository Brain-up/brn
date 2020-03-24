package com.epam.brn.upload.csv

import com.epam.brn.upload.csv.converter.Converter
import com.epam.brn.upload.csv.iterator.MappingIteratorProvider
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class CsvParser {

    val log = logger()

    companion object {
        const val ARRAY_OFFSET = -1
    }

    final inline fun <reified ParsedType, reified ConvertedType> parse(
        inputStream: InputStream,
        converter: Converter<ParsedType, ConvertedType>,
        mappingIteratorProvider: MappingIteratorProvider<ParsedType>
    ): List<ConvertedType> {
        ByteArrayInputStream(IOUtils.toByteArray(inputStream)).use {
            val parsed = mutableListOf<ParsedType>()
            val errors = mutableListOf<String>()

            val originalLines = getOriginalLines(it)

            val parsingIterator = mappingIteratorProvider.iterator(it)
            while (parsingIterator.hasNextValue()) {
                val lineNumberInFile = parsingIterator.currentLocation.lineNr

                val originalValue = originalLines[lineNumberInFile + ARRAY_OFFSET]
                try {
                    parsed.add(parsingIterator.nextValue())
                    log.debug("Successfully parsed line $lineNumberInFile: '$originalValue'.")
                } catch (e: Exception) {
                    errors.add("Failed to parse line $lineNumberInFile: '$originalValue'. Error: ${e.localizedMessage}")
                    log.debug("Failed to parse line $lineNumberInFile ", e)
                }
            }
            if (errors.isNotEmpty()) throw ParseException(errors)

            return parsed.map { parsedValue -> converter.convert(parsedValue) }
        }
    }

    fun getOriginalLines(inputStream: InputStream): MutableList<String> {
        val originalLines = BufferedReader(InputStreamReader(inputStream))
            .lines()
            .collect(Collectors.toList())
        inputStream.reset()
        return originalLines
    }

    class ParseException(val errors: List<String>) :
        RuntimeException("Parsing error. Please check csv file content format.")
}
