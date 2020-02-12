package com.epam.brn.job.csv.task.impl

import com.epam.brn.job.csv.task.UploadFromCsvStrategy
import com.epam.brn.model.Exercise
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.parsers.csv.CsvMappingIteratorParser
import com.epam.brn.service.parsers.csv.converter.impl.secondSeries.SecondSeriesMapToExerciseModelConverter
import com.epam.brn.service.parsers.csv.secondSeries.SecondSeriesCSVParserService
import java.io.InputStream
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UploadFromCsvServiceSecondSeriesStrategy(private val csvMappingIteratorParser: CsvMappingIteratorParser) :
    UploadFromCsvStrategy {

    private val log = logger()

    @Autowired
    private lateinit var secondSeriesMapToExerciseModelConverter: SecondSeriesMapToExerciseModelConverter

    @Autowired
    private lateinit var secondSeriesCSVParserService: SecondSeriesCSVParserService

    @Autowired
    private lateinit var exerciseService: ExerciseService

    override fun uploadFile(inputStream: InputStream): Map<String, String> {
        val exercises = csvMappingIteratorParser.parseCsvFile(inputStream, secondSeriesMapToExerciseModelConverter, secondSeriesCSVParserService)
        return saveExercises(exercises)
    }

    private fun saveExercises(exercises: Map<String, Pair<Exercise?, String?>>): Map<String, String> {
        val notSavingExercises = mutableMapOf<String, String>()

        exercises.forEach {
            val key = it.key
            val exercise = it.value.first
            try {
                if (exercise != null)
                    exerciseService.save(exercise)
                else
                    it.value.second?.let { errorMessage -> notSavingExercises[key] = errorMessage }
            } catch (e: Exception) {
                notSavingExercises[key] = e.localizedMessage
                log.warn("Failed to insert : $key ", e)
            }
            log.debug("Successfully inserted line: $key")
        }
        return notSavingExercises
    }
}
