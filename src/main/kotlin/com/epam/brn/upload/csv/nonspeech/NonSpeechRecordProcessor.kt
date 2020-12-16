package com.epam.brn.upload.csv.nonspeech

import com.epam.brn.model.Exercise
import com.epam.brn.model.Signal
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.upload.csv.RecordProcessor
import org.springframework.stereotype.Component

@Component
class NonSpeechRecordProcessor(
    private val exerciseRepository: ExerciseRepository,
    private val seriesRepository: SeriesRepository
) : RecordProcessor<NonSpeechRecord, Exercise> {
    override fun isApplicable(record: Any): Boolean {
        return record is NonSpeechRecord
    }

    override fun process(records: List<NonSpeechRecord>): List<Exercise> {
        val seriesList = records.map { it.series }.distinct()
        val seriesMap = seriesRepository.findByNameIn(seriesList).associateBy { it.name }
        val exercises = records.map { record ->
            val series = seriesMap[record.series] ?: throw RuntimeException()
            val exercise = Exercise(series = series, name = record.exerciseName, level = record.level,
                exerciseType = record.exerciseType.name)
            val signals = record.signals.map { signalValue ->
                val signalValues = signalValue.split(" ").map { it.toInt() }
                Signal(frequency = signalValues[0], length = signalValues[1], exercise = exercise)
            }
            exercise.addSignals(signals)
            exercise
            }
        return exerciseRepository.saveAll(exercises)
    }
}
