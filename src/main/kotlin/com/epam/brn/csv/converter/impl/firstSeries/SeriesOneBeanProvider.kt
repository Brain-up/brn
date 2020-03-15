package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.csv.converter.CsvToEntityConverter
import com.epam.brn.csv.converter.DataLoadingBeanProvider
import com.epam.brn.csv.converter.ObjectReaderProvider
import com.epam.brn.csv.dto.TaskCsv
import com.epam.brn.model.Task
import com.epam.brn.repo.TaskRepository
import org.springframework.stereotype.Component

@Component
class SeriesOneBeanProvider(
    private val repository: TaskRepository,
    private val objectReaderProvider: ObjectReaderProvider<TaskCsv>,
    private val converter: CsvToEntityConverter<TaskCsv, Task>
) : DataLoadingBeanProvider<TaskCsv, Task> {
    override fun shouldProcess(fileName: String) = "1_series.csv" == fileName
    override fun converter() = converter
    override fun objectReaderProvider() = objectReaderProvider
    override fun repository() = repository
}
