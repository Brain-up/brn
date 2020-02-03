package com.epam.brn.job.csv.task.impl

import com.epam.brn.constant.BrnErrors.CSV_FILE_FORMAT_ERROR
import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.CsvUtils
import com.epam.brn.job.csv.task.UploadFromCsvService
import com.epam.brn.model.Task
import com.epam.brn.service.ExerciseGroupsService
import com.epam.brn.service.SeriesService
import com.epam.brn.service.TaskService
import com.epam.brn.service.parsers.csv.FirstSeriesCSVParserService
import com.epam.brn.service.parsers.csv.converter.impl.TaskCsvToTaskModelConverter
import java.io.File
import java.io.InputStream
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class UploadTaskFromCsvService(private val firstSeriesCsvParserService: FirstSeriesCSVParserService, private val taskService: TaskService) :
    UploadFromCsvService {
    private val log = logger()

    @Autowired
    private lateinit var taskCsvToTaskModelConverter: TaskCsvToTaskModelConverter

    @Autowired
    private lateinit var seriesService: SeriesService

    @Autowired
    private lateinit var exerciseGroupsService: ExerciseGroupsService

    @Throws(FileFormatException::class)
    override fun loadTaskFile(file: MultipartFile, seriesId: Long?): Map<String, String> {
        if (!isFileContentTypeCsv(file.contentType ?: StringUtils.EMPTY)) throw FileFormatException(CSV_FILE_FORMAT_ERROR)

        return uploadTasks(file.inputStream, seriesId)
    }

    @Throws(FileFormatException::class)
    override fun loadTaskFile(file: File): Map<String, String> {
        return uploadTasks(file.inputStream(), null)
    }

    private fun uploadTasks(inputStream: InputStream, seriesId: Long?): Map<String, String> {
        val tasks = firstSeriesCsvParserService.parseCsvFile(inputStream, taskCsvToTaskModelConverter)

        if (seriesId != null) tasks.forEach { task -> setExerciseSeries(task.value.first, seriesId) }

        return saveTasks(tasks)
    }

    private fun setExerciseSeries(taskFile: Task?, seriesId: Long) {
        taskFile?.exercise?.series = seriesService.findSeriesForId(seriesId)
    }

    private fun isFileContentTypeCsv(contentType: String): Boolean {
        return CsvUtils.isFileContentTypeCsv(contentType)
    }

    private fun saveTasks(tasks: Map<String, Pair<Task?, String?>>): Map<String, String> {
        val notSavingTasks = mutableMapOf<String, String>()

        tasks.forEach {
            val key = it.key
            val task = it.value.first

            try {
                if (task != null) {
                    taskService.save(task)
                } else {
                    it.value.second?.let { errorMessage -> notSavingTasks[key] = errorMessage }
                }
            } catch (e: Exception) {
                notSavingTasks[key] = e.localizedMessage
                log.warn("Failed to insert : $key ", e)
            }

            log.debug("Successfully inserted line: $key")
        }

        return notSavingTasks
    }
}
