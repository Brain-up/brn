package com.epam.brn.upload.csv.parser

import com.epam.brn.upload.csv.parser.iterator.MappingIteratorProvider
import com.epam.brn.upload.csv.parser.iterator.impl.GroupRecordMappingIteratorProvider
import com.epam.brn.upload.csv.parser.iterator.impl.SeriesGenericRecordMappingIteratorProvider
import com.epam.brn.upload.csv.parser.iterator.impl.SeriesOneRecordMappingIteratorProvider
import com.epam.brn.upload.csv.parser.iterator.impl.SeriesThreeRecordMappingIteratorProvider
import com.epam.brn.upload.csv.parser.iterator.impl.SeriesTwoRecordMappingIteratorProvider
import com.epam.brn.upload.csv.record.CsvRecord
import com.epam.brn.upload.csv.record.GroupRecord
import com.epam.brn.upload.csv.record.SeriesGenericRecord
import com.epam.brn.upload.csv.record.SeriesOneRecord
import com.epam.brn.upload.csv.record.SeriesThreeRecord
import com.epam.brn.upload.csv.record.SeriesTwoRecord
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

    fun parse(inputStream: InputStream): List<CsvRecord> {
        ByteArrayInputStream(IOUtils.toByteArray(inputStream)).use {
            val parsed = mutableListOf<CsvRecord>()
            val errors = mutableListOf<String>()

            val originalLines = readOriginalLines(it)

            val iteratorProvider = getProvider(originalLines.first())
            val parsingIterator = iteratorProvider.iterator(it)
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
            if (errors.isNotEmpty()) throw ParseException(
                errors
            )

            return parsed
        }
    }

    fun readOriginalLines(inputStream: InputStream): MutableList<String> {
        val originalLines = BufferedReader(InputStreamReader(inputStream))
            .lines()
            .collect(Collectors.toList())
        inputStream.reset()
        return originalLines
    }

    private fun getProvider(header: String): MappingIteratorProvider<out CsvRecord> {
        return when (header) {
            GroupRecord.HEADER -> GroupRecordMappingIteratorProvider()
            SeriesGenericRecord.HEADER -> SeriesGenericRecordMappingIteratorProvider()
            SeriesOneRecord.HEADER -> SeriesOneRecordMappingIteratorProvider()
            SeriesTwoRecord.HEADER -> SeriesTwoRecordMappingIteratorProvider()
            SeriesThreeRecord.HEADER -> SeriesThreeRecordMappingIteratorProvider()
            else -> throw ParseException()
        }
    }

    class ParseException() : RuntimeException("Parsing error. Please check csv file content format.") {

        lateinit var errors: List<String>

        constructor(errors: List<String>) : this() {
            this.errors = errors
        }
    }
}
