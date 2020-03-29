package com.epam.brn.upload

import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.CsvUtils
import com.epam.brn.model.Exercise
import com.epam.brn.service.InitialDataLoader
import com.epam.brn.upload.csv.parser.CsvParser
import com.epam.brn.upload.csv.processor.RecordProcessor
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.LineNumberReader
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class CsvUploadService(
    private val csvParser: CsvParser,
    private val recordProcessors: List<RecordProcessor<out Any, out Any>>
) {

    @Value("\${brn.dataFormatNumLines}")
    val dataFormatLinesCount = 5

    @Suppress("UNCHECKED_CAST")
    fun load(inputStream: InputStream): List<Any> {
        val records = csvParser.parse(inputStream)

        return recordProcessors.stream()
            .filter { it.isApplicable(records.first()) }
            .findFirst()
            .orElseThrow {
                RuntimeException("There is no applicable processor for type '${records.first().javaClass}'")
            }
            .process(records as List<Nothing>)
    }

    @Throws(FileFormatException::class)
    fun loadExercises(seriesId: Long, file: MultipartFile): List<Exercise> {

        if (!isFileContentTypeCsv(file.contentType ?: StringUtils.EMPTY))
            throw FileFormatException()

        @Suppress("UNCHECKED_CAST")
        return when (seriesId.toInt()) {
            1, 2, 3 -> load(file.inputStream) as List<Exercise>
            else -> throw IllegalArgumentException("Loading for seriesId = $seriesId is not supported yet.")
        }
    }

    private fun isFileContentTypeCsv(contentType: String): Boolean = CsvUtils.isFileContentTypeCsv(contentType)

    @Suppress("UNCHECKED_CAST")
    @Throws(FileFormatException::class)
    fun loadTasks(file: File): List<Exercise> = load(file.inputStream()) as List<Exercise>

    fun getSampleStringForSeriesFile(seriesId: Long): String {
        return readFormatSampleLines(InitialDataLoader.getInputStreamFromSeriesInitFile(seriesId))
    }

    private fun readFormatSampleLines(inputStream: InputStream): String {
        return getLinesFrom(inputStream, dataFormatLinesCount).joinToString("\n")
    }

    private fun getLinesFrom(inputStream: InputStream, linesCount: Int): MutableList<String> {
        inputStream.use {
            val strings = mutableListOf<String>()

            val reader = LineNumberReader(InputStreamReader(inputStream))
            while (reader.lineNumber < linesCount) {
                reader.readLine().let { strings.add(it) }
            }
            return strings
        }
    }
}
