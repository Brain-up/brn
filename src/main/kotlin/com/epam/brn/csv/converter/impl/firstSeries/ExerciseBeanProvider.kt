package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.csv.converter.CsvToEntityConverter
import com.epam.brn.csv.converter.ObjectReaderProvider
import com.epam.brn.csv.converter.DataLoadingBeanProvider
import com.epam.brn.csv.dto.ExerciseCsv
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import org.springframework.stereotype.Component

// TODO: delete this class and exercises.csv, for now tests are breaking
@Component
class ExerciseBeanProvider(
    private val repository: ExerciseRepository,
    private val objectReaderProvider: ObjectReaderProvider<ExerciseCsv>,
    private val converter: CsvToEntityConverter<ExerciseCsv, Exercise>
) : DataLoadingBeanProvider<ExerciseCsv, Exercise> {

    override fun shouldProcess(fileName: String) = "exercises.csv" == fileName
    override fun repository() = repository
    override fun objectReaderProvider() = objectReaderProvider
    override fun converter() = converter
}
