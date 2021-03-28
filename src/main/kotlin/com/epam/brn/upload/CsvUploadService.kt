package com.epam.brn.upload

import com.epam.brn.enums.Locale
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.exception.FileFormatException
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.service.load.InitialDataLoader
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
    private val recordProcessors: List<RecordProcessor<out Any, out Any>>,
    private val seriesRepository: SeriesRepository
) {

    val localeSuffixMap = mapOf("ru" to Locale.RU, "en" to Locale.EN, "tr" to Locale.TR)

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
    fun load(inputStream: InputStream, locale: Locale) {
        val records = csvParser.parse(inputStream)
        recordProcessors.stream()
            .filter { it.isApplicable(records.first()) }.findFirst()
            .orElseThrow {
                RuntimeException("There is no applicable processor for type '${records.first().javaClass}'")
            }
            .process(records as List<Nothing>, locale)
    }

    @Throws(FileFormatException::class)
    fun loadExercises(seriesId: Long, file: MultipartFile) {

        if (isNotCsvContentType(file.contentType))
            throw FileFormatException()

        @Suppress("UNCHECKED_CAST")
        when (seriesId.toInt()) {
            1, 2, 3, 4 -> load(file.inputStream, Locale.RU)
            else -> throw IllegalArgumentException("Loading for seriesId = $seriesId is not supported yet.")
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(FileFormatException::class)
    fun load(file: File) = load(file.inputStream(), getLocaleFromFileName(file.nameWithoutExtension))

    fun getLocaleFromFileName(fileNameWithExtension: String): Locale {
        val fileName = fileNameWithExtension.substringBefore(".")
        val suffix = if (fileName.endsWith("_")) "" else fileName.substring(fileName.length - 2)
        return when {
            suffix.isEmpty() -> Locale.RU
            localeSuffixMap[suffix] == null -> throw IllegalArgumentException("There no supported locale for $suffix file. Ask to tech support.")
            else -> localeSuffixMap[suffix]!!
        }
    }

    fun getSampleStringForSeriesExerciseFile(seriesId: Long): String {
        val type = seriesRepository.findById(seriesId)
            .orElseThrow { EntityNotFoundException("There no any series with id = $seriesId") }
            .type
        return readFormatSampleLines(InitialDataLoader.getInputStreamFromSeriesInitFile(type))
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
