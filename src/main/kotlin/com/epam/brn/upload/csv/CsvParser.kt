package com.epam.brn.upload.csv

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class CsvParser(val iteratorProviders: List<MappingIteratorProvider<out Any>>) {

    val log = logger()

    companion object {
        const val ARRAY_OFFSET = -1
    }

    fun parse(inputStream: InputStream): List<Any> {
        ByteArrayInputStream(IOUtils.toByteArray(inputStream)).use {
            val parsed = mutableListOf<Any>()
            val errors = mutableListOf<String>()

            val originalLines = readOriginalLines(it)

            val iteratorProvider = getProvider(originalLines.first())
            val parsingIterator = iteratorProvider.iterator(it)
            try {
                while (parsingIterator.hasNextValue()) {
                    val lineNumberInFile = parsingIterator.currentLocation.lineNr

                    val originalValue = originalLines[lineNumberInFile + ARRAY_OFFSET]
                    try {
                        parsed.add(parsingIterator.nextValue())
                        log.debug("Successfully parsed line $lineNumberInFile: '$originalValue'.")
                    } catch (e: Exception) {
                        errors.add(
                            "Failed to parse line $lineNumberInFile: '$originalValue'. Error: ${e.localizedMessage}"
                        )
                        log.debug("Failed to parse line $lineNumberInFile", e)
                    }
                }
            } catch (e: Exception) {
                errors.add("Parse error: ${e.localizedMessage}")
                log.debug(e)
            }
            if (errors.isNotEmpty()) throw ParseException(errors)

            return parsed
        }
    }

    fun readOriginalLines(inputStream: InputStream): MutableList<String> {
        val originalLines = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            .lines()
            .collect(Collectors.toList())
        inputStream.reset()
        return originalLines
    }

    private fun getProvider(header: String): MappingIteratorProvider<out Any> {
        return iteratorProviders.stream()
            .filter { it.isApplicable(header) }
            .findFirst()
            .orElseThrow { ParseException("There is no applicable iterator provider for format '$header'.") }
    }

    class ParseException(message: String = "Parsing error. Please check csv file content format.") :
        RuntimeException(message) {

        lateinit var errors: List<String>

        constructor(errors: List<String>) : this() {
            this.errors = errors
        }
    }
}
