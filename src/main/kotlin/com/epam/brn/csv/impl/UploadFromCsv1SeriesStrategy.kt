package com.epam.brn.csv.impl

import com.epam.brn.csv.CsvMappingIteratorParser
import com.epam.brn.csv.UploadFromCsvStrategy
import com.epam.brn.csv.converter.impl.firstSeries.TaskCsv1SeriesConverter
import com.epam.brn.csv.firstSeries.TaskCSVParser1SeriesService
import com.epam.brn.model.Task
import com.epam.brn.service.SeriesService
import com.epam.brn.service.TaskService
import java.io.InputStream
import org.springframework.stereotype.Component

@Component
class UploadFromCsv1SeriesStrategy(
    private val csvMappingIteratorParser: CsvMappingIteratorParser,
    private val taskService: TaskService,
    private val taskCsv1SeriesConverter: TaskCsv1SeriesConverter,
    private val seriesService: SeriesService
) : UploadFromCsvStrategy {

    override fun uploadFile(inputStream: InputStream): List<Task> {
        val result = csvMappingIteratorParser
            .parseCsvFile(inputStream, taskCsv1SeriesConverter, TaskCSVParser1SeriesService())

        result.forEach { task -> setExerciseSeries(task) }

        return taskService.save(result)
    }

    private fun setExerciseSeries(taskFile: Task?) {
        taskFile?.exercise?.series = seriesService.findSeriesForId(1)
    }
}
