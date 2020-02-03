package com.epam.brn.service.parsers.csv

import com.epam.brn.service.parsers.csv.converter.Converter
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors.toList
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils.LONG_ONE
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class FirstSeriesCSVParserService {

    val log = logger()

    /**
     * @param converter - converter from csv dto to model
     * @param file - csv-file which should be convert to model
     *
     * @return csv-line to pair of model object and error. One pair values must be empty.
     */
    final inline fun <reified Source, reified Target> parseCsvFile(file: InputStream, converter: Converter<Source, Target>): Map<String, Pair<Target?, String?>> {
        ByteArrayInputStream(IOUtils.toByteArray(file)).use {
            return parseCsvFile(it, converter)
        }
    }

    final inline fun <reified Source, reified Target> parseCommasSeparatedCsvFile(file: InputStream, converter: Converter<Source, Target>): List<Target> {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(Source::class.java)
            .withColumnSeparator(',')
            .withLineSeparator(StringUtils.SPACE)
            .withColumnReordering(true)
            .withHeader()

        return csvMapper
            .readerWithTypedSchemaFor(Source::class.java)
            .with(csvSchema)
            .readValues<Source>(file)
            .readAll()
            .map(converter::convert)
    }

    final inline fun <reified Source, reified Target> parseCsvFile(file: ByteArrayInputStream, converter: Converter<Source, Target>): Map<String, Pair<Target?, String?>> {
        val csvLineNumbersToValues = getCsvLineNumbersToValues(file)

        val csvMapper = CsvMapper()

        val csvSchema = csvMapper
            .schemaFor(Source::class.java)
            .withColumnSeparator(' ')
            .withLineSeparator(StringUtils.SPACE)
            .withColumnReordering(true)
            .withArrayElementSeparator(",")
            .withHeader()

        val readValues = csvMapper
            .readerWithTypedSchemaFor(Source::class.java)
            .with(csvSchema)
            .readValues<Source>(file)

        val parsedValues = hashMapOf<String, Source>()
        val sourceToTarget = hashMapOf<String, Pair<Target?, String?>>()

        while (readValues.hasNextValue()) {
            val lineNumber = readValues.currentLocation.lineNr
            try {
                val line = readValues.nextValue()
                csvLineNumbersToValues[lineNumber]?.let {
                    parsedValues[it] = line
                }

                log.debug("Successfully parsed line with number $lineNumber")
            } catch (e: Exception) {
                csvLineNumbersToValues[lineNumber]?.let {
                    sourceToTarget[it] = Pair(null, "Parse Exception - wrong format: ${e.localizedMessage}")
                }

                log.error("Failed to parse line with number $lineNumber ", e)
            }
        }

        sourceToTarget.putAll(parsedValues
            .map { parsedValue -> parsedValue.key to Pair(converter.convert(parsedValue.value), null) }
            .toMap())

        return sourceToTarget
    }

    fun getCsvLineNumbersToValues(file: InputStream): Map<Int, String> {
        val reader = BufferedReader(InputStreamReader(file))

        val result = mutableMapOf<Int, String>()
        val listOfLinesWithoutHeader = reader
            .lines()
            .skip(LONG_ONE)
            .collect(toList())
        listOfLinesWithoutHeader.forEachIndexed { index, s ->
            result[index + 2] = s
        }

        file.reset()
        return result
    }
}
