package com.epam.brn.csv.impl

import com.epam.brn.csv.CsvMappingIteratorParser
import com.epam.brn.csv.UploadFromCsvStrategy
import com.epam.brn.csv.converter.impl.secondSeries.Exercise2SeriesConverter
import com.epam.brn.csv.secondSeries.CSVParser2SeriesService
import com.epam.brn.model.Exercise
import com.epam.brn.service.ExerciseService
import java.io.InputStream
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Component

@Component
class UploadFromCsv2SeriesStrategy(
    private val csvMappingIteratorParser: CsvMappingIteratorParser,
    private val exercise2SeriesConverter: Exercise2SeriesConverter,
    private val csvParser2SeriesService: CSVParser2SeriesService,
    private val exerciseService: ExerciseService
) :
    UploadFromCsvStrategy {

    private val log = logger()

    override fun uploadFile(inputStream: InputStream): Map<String, String> {
        val exercises = csvMappingIteratorParser
            .parseCsvFile(inputStream, exercise2SeriesConverter, csvParser2SeriesService)

        return saveExercises(exercises)
    }

    private fun saveExercises(exercises: Map<String, Pair<Exercise?, String?>>): Map<String, String> {
        val unsavedExercises = mutableMapOf<String, String>()

        exercises.forEach {
            val key = it.key
            val exercise = it.value.first
            try {
                if (exercise != null)
                    exerciseService.save(exercise)
                else
                    it.value.second?.let { errorMessage -> unsavedExercises[key] = errorMessage }
            } catch (e: Exception) {
                unsavedExercises[key] = e.localizedMessage
                log.warn("Failed to insert : $key ", e)
            }
            log.debug("Successfully inserted line: $key")
        }
        return unsavedExercises
    }
}
