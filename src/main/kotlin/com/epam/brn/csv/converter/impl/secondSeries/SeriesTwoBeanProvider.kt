package com.epam.brn.csv.converter.impl.secondSeries

import com.epam.brn.csv.converter.CsvToEntityConverter
import com.epam.brn.csv.converter.ObjectReaderProvider
import com.epam.brn.csv.converter.DataLoadingBeanProvider
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import org.springframework.stereotype.Component

@Component
class SeriesTwoBeanProvider(
    private val repository: ExerciseRepository,
    private val objectReaderProvider: ObjectReaderProvider<Map<String, Any>>,
    private val converter: CsvToEntityConverter<Map<String, Any>, Exercise>
) : DataLoadingBeanProvider<Map<String, Any>, Exercise> {
    override fun shouldProcess(fileName: String) = "2_series.csv" == fileName
    override fun repository() = repository
    override fun objectReaderProvider() = objectReaderProvider
    override fun converter() = converter
}
