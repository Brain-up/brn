package com.epam.brn.csv.impl

import com.epam.brn.csv.CsvMappingIteratorParser
import com.epam.brn.csv.UploadFromCsvStrategy
import com.epam.brn.csv.converter.impl.secondSeries.Exercise2SeriesConverter
import com.epam.brn.csv.secondSeries.CSVParser2SeriesService
import com.epam.brn.model.Exercise
import com.epam.brn.service.ExerciseService
import java.io.InputStream
import org.springframework.stereotype.Component

@Component
class UploadFromCsv2SeriesStrategy(
    private val csvMappingIteratorParser: CsvMappingIteratorParser,
    private val exercise2SeriesConverter: Exercise2SeriesConverter,
    private val exerciseService: ExerciseService
) :
    UploadFromCsvStrategy {

    override fun uploadFile(inputStream: InputStream): List<Exercise> {
        val exercises = csvMappingIteratorParser
            .parseCsvFile(inputStream, exercise2SeriesConverter, CSVParser2SeriesService())

        return exerciseService.save(exercises)
    }
}
