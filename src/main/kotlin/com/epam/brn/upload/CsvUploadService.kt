package com.epam.brn.upload

import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.CsvUtils
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.service.InitialDataLoader
import com.epam.brn.upload.csv.parser.CsvParser
import com.epam.brn.upload.csv.processor.GroupRecordProcessor
import com.epam.brn.upload.csv.processor.SeriesGenericRecordProcessor
import com.epam.brn.upload.csv.processor.SeriesOneExerciseRecordProcessor
import com.epam.brn.upload.csv.processor.SeriesThreeRecordProcessor
import com.epam.brn.upload.csv.processor.SeriesTwoExerciseRecordProcessor
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
    private val groupRecordProcessor: GroupRecordProcessor,
    private val seriesGenericRecordProcessor: SeriesGenericRecordProcessor,
    private val seriesOneExerciseRecordProcessor: SeriesOneExerciseRecordProcessor,
    private val seriesTwoExerciseRecordProcessor: SeriesTwoExerciseRecordProcessor,
    private val seriesThreeRecordProcessor: SeriesThreeRecordProcessor
) {

    @Value("\${brn.dataFormatNumLines}")
    val dataFormatLinesCount = 5

    fun loadGroups(inputStream: InputStream): Iterable<ExerciseGroup> {
        val records = csvParser.parseGroupRecords(inputStream)

        return groupRecordProcessor.process(records)
    }

    fun loadSeries(inputStream: InputStream): Iterable<Series> {
        val records = csvParser.parseSeriesGenericRecords(inputStream)

        return seriesGenericRecordProcessor.process(records)
    }

    @Throws(FileFormatException::class)
    fun loadExercises(seriesId: Long, file: MultipartFile): List<Any> {

        if (!isFileContentTypeCsv(file.contentType ?: StringUtils.EMPTY))
            throw FileFormatException()

        return when (seriesId.toInt()) {
            1 -> loadTasksFor1Series(file.inputStream)
            2 -> loadExercisesFor2Series(file.inputStream)
            3 -> loadExercisesFor3Series(file.inputStream)
            else -> throw IllegalArgumentException("There no one strategy yet for seriesId = $seriesId")
        }
    }

    private fun isFileContentTypeCsv(contentType: String): Boolean = CsvUtils.isFileContentTypeCsv(contentType)

    @Throws(FileFormatException::class)
    fun loadTasks(file: File): List<Task> = loadTasksFor1Series(file.inputStream())

    fun loadTasksFor1Series(inputStream: InputStream): List<Task> {
        val records = csvParser.parseSeriesOneExerciseRecords(inputStream)

        return seriesOneExerciseRecordProcessor.process(records)
    }

    fun loadExercisesFor2Series(inputStream: InputStream): List<Exercise> {
        val records = csvParser.parseSeriesTwoExerciseRecords(inputStream)

        return seriesTwoExerciseRecordProcessor.process(records)
    }

    fun loadExercisesFor3Series(inputStream: InputStream): List<Exercise> {
        val records = csvParser.parseSeriesThreeExerciseRecords(inputStream)

        return seriesThreeRecordProcessor.process(records)
    }

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
