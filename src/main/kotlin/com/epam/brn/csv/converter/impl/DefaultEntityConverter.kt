package com.epam.brn.csv.converter.impl

import com.epam.brn.csv.converter.CsvToEntityConverter
import com.epam.brn.csv.converter.StringToEntityConverter
import com.fasterxml.jackson.databind.MappingIterator
import java.util.Optional
import java.util.stream.Stream
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class DefaultEntityConverter : StringToEntityConverter {

    val log = logger()

    override fun <Csv, Entity> toEntity(
        rawCsvByLine: Stream<String>,
        mappingIterator: MappingIterator<Csv>,
        converter: CsvToEntityConverter<Csv, Entity>
    ): Stream<DataConversionResult<Entity>> {
        val csvConversionResults = parseCsvFile(rawCsvByLine, mappingIterator)
        return extractEntityFromCsv(csvConversionResults, converter)
    }

    private fun <Csv, Entity> extractEntityFromCsv(
        csvConversions: Stream<DataConversionResult<Csv>>,
        converter: CsvToEntityConverter<Csv, Entity>
    ): Stream<DataConversionResult<Entity>> {
        return csvConversions.map {
            if (it.error.isPresent) {
                DataConversionResult(
                    it.index,
                    it.line,
                    Optional.empty(),
                    Optional.of(it.error.get())
                )
            } else {
                try {
                    val entity = converter.convert(it.data.get())
                    DataConversionResult(
                        it.index,
                        it.line,
                        Optional.of(entity),
                        Optional.empty()
                    )
                } catch (exception: Exception) {
                    log.error("Failed to convert entity from csv for data : $it ", exception)
                    DataConversionResult<Entity>(
                        it.index,
                        it.line,
                        Optional.empty(),
                        Optional.of("Failed to convert entity from csv for data : $it")
                    )
                }
            }
        }
    }

    override fun <Csv> parseCsvFile(
        rawCsvByLine: Stream<String>,
        mappingIterator: MappingIterator<Csv>
    ): Stream<DataConversionResult<Csv>> {
        return rawCsvByLine.map {
            val lineNumber = mappingIterator.currentLocation.lineNr
            if (mappingIterator.hasNextValue()) {
                parseNextCsvValue(it, mappingIterator, lineNumber)
            } else {
                log.error("Mapping iterator out of index for data : $it")
                DataConversionResult(
                    lineNumber,
                    it,
                    Optional.empty(),
                    Optional.of("Mapping iterator out of index for data : $it")
                )
            }
        }
    }

    fun <Csv> parseNextCsvValue(line: String, iterator: MappingIterator<Csv>, lineNumber: Int): DataConversionResult<Csv> {
        return try {
            val csv = iterator.nextValue()
            log.debug("Successfully parsed line with number $lineNumber")
            DataConversionResult(lineNumber, line, Optional.of(csv), Optional.empty())
        } catch (e: Exception) {
            log.error("Failed to parse line with number $lineNumber, content: $line ", e)
            DataConversionResult(
                lineNumber,
                line,
                Optional.empty(),
                Optional.of("Parse Exception - wrong format: ${e.localizedMessage}")
            )
        }
    }
}
