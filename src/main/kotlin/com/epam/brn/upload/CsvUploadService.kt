package com.epam.brn.upload

import com.epam.brn.exception.FileFormatException
import com.epam.brn.service.InitialDataLoader
import com.epam.brn.upload.csv.CsvParser
import com.epam.brn.upload.csv.RecordProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.LineNumberReader

@Component
class CsvUploadService(
    private val csvParser: CsvParser,
    private val recordProcessors: List<RecordProcessor<out Any, out Any>>
) {
    companion object {

        private val csvContentTypes = listOf(
            "text/csv",
            "application/vnd.ms-excel",
            "text/plain",
            "text/tsv",
            "application/octet-stream"
        )

        fun isCsvContentType(contentType: String?): Boolean {
            return contentType != null && csvContentTypes.contains(contentType)
        }

        fun isNotCsvContentType(contentType: String?): Boolean {
            return !isCsvContentType(contentType)
        }
    }

    @Value("\${brn.dataFormatNumLines}")
    val dataFormatLinesCount = 5

    @Suppress("UNCHECKED_CAST")
    fun load(inputStream: InputStream) {
        val records = csvParser.parse(inputStream)

        recordProcessors.stream()
            .filter { it.isApplicable(records.first()) }.findFirst()
            .orElseThrow {
                RuntimeException("There is no applicable processor for type '${records.first().javaClass}'")
            }
            .process(records as List<Nothing>)
    }

    @Throws(FileFormatException::class)
    fun loadExercises(seriesId: Long, file: MultipartFile) {

        if (isNotCsvContentType(file.contentType))
            throw FileFormatException()

        @Suppress("UNCHECKED_CAST")
        when (seriesId.toInt()) {
            1, 2, 3, 4 -> load(file.inputStream)
            else -> throw IllegalArgumentException("Loading for seriesId = $seriesId is not supported yet.")
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(FileFormatException::class)
    fun load(file: File) = load(file.inputStream())

    fun getSampleStringForSeriesExerciseFile(seriesId: Long): String {
        return readFormatSampleLines(InitialDataLoader.getInputStreamFromSeriesInitFile(seriesId))
    }

    private fun readFormatSampleLines(inputStream: InputStream): String {
        return getLinesFrom(inputStream, dataFormatLinesCount).joinToString("\n")
    }

    private fun getLinesFrom(inputStream: InputStream, linesCount: Int): MutableList<String> {
        inputStream.use {
            val strings = mutableListOf<String>()

            val reader = LineNumberReader(InputStreamReader(inputStream, Charsets.UTF_8))
            while (reader.lineNumber < linesCount) {
                reader.readLine().let { strings.add(it) }
            }
            return strings
        }
    }
}
