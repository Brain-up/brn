package com.epam.brn.csv.impl

import com.epam.brn.csv.CsvMappingIteratorParser
import com.epam.brn.csv.UploadFromCsvStrategy
import com.epam.brn.csv.converter.impl.firstSeries.TaskCsv1SeriesConverter
import com.epam.brn.csv.firstSeries.TaskCSVParser1SeriesService
import com.epam.brn.model.Task
import com.epam.brn.service.SeriesService
import com.epam.brn.service.TaskService
import java.io.InputStream
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Component

@Component
class UploadFromCsv1SeriesStrategy(
    private val csvMappingIteratorParser: CsvMappingIteratorParser,
    private val taskService: TaskService,
    private val taskCsv1SeriesConverter: TaskCsv1SeriesConverter,
    private val seriesService: SeriesService,
    private val taskCSVParser1SeriesService: TaskCSVParser1SeriesService
) : UploadFromCsvStrategy {
    private val log = logger()

    override fun uploadFile(inputStream: InputStream): Map<String, String> {
        val tasks = csvMappingIteratorParser.parseCsvFile(inputStream, taskCsv1SeriesConverter, taskCSVParser1SeriesService)
        tasks.forEach { task -> setExerciseSeries(task.value.first) }
        return saveTasks(tasks)
    }

    private fun setExerciseSeries(taskFile: Task?) {
        taskFile?.exercise?.series = seriesService.findSeriesForId(1)
    }

    private fun saveTasks(tasks: Map<String, Pair<Task?, String?>>): Map<String, String> {
        val notSavingTasks = mutableMapOf<String, String>()

        tasks.forEach {
            val key = it.key
            val task = it.value.first
            try {
                if (task != null)
                    taskService.save(task)
                else
                    it.value.second?.let { errorMessage -> notSavingTasks[key] = errorMessage }
            } catch (e: Exception) {
                notSavingTasks[key] = e.localizedMessage
                log.warn("Failed to insert : $key ", e)
            }
            log.debug("Successfully inserted line: $key")
        }
        return notSavingTasks
    }
}
