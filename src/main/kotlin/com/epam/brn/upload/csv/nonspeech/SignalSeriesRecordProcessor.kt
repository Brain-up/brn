package com.epam.brn.upload.csv.nonspeech

import com.epam.brn.enums.Locale
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Signal
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.upload.csv.RecordProcessor
import org.springframework.stereotype.Component

@Component
class SignalSeriesRecordProcessor(
    private val subGroupRepository: SubGroupRepository,
    private val exerciseRepository: ExerciseRepository
) : RecordProcessor<SignalSeriesRecord, Exercise> {
    override fun isApplicable(record: Any): Boolean {
        return record is SignalSeriesRecord
    }

    override fun process(records: List<SignalSeriesRecord>, locale: Locale): List<Exercise> {
        val exercises = records.map { record ->
            val subGroup = subGroupRepository.findByCodeAndLocale(record.code, locale.locale)
                ?: throw EntityNotFoundException("No subGroup was found for code=${record.code} and locale={${locale.locale}}")
            val exercise = Exercise(
                subGroup = subGroup,
                name = record.exerciseName,
                level = record.level
            )
            val signals = record.signals.map { signalValue ->
                val signalValues = signalValue.split(" ").map { it.toInt() }
                Signal(frequency = signalValues[0], length = signalValues[1], exercise = exercise)
            }
            exercise.addSignals(signals)
            exercise
        }
        exercises.forEach { exercise ->
            run {
                val existExercise = exerciseRepository.findByNameAndLevel(exercise.name, exercise.level)
                if (existExercise == null)
                    exerciseRepository.save(exercise)
            }
        }
        return exercises
    }
}
