package com.epam.brn.upload.csv.series1

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.model.WordType
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.upload.csv.RecordProcessor
import java.util.Random
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SeriesOneRecordProcessor(
    private val seriesRepository: SeriesRepository,
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository
) : RecordProcessor<SeriesOneRecord, Exercise> {

    @Value(value = "\${brn.picture.file.default.path}")
    private lateinit var pictureDefaultPath: String

    private val repeatCount = 2

    var random = Random()

    override fun isApplicable(record: Any): Boolean {
        return record is SeriesOneRecord
    }

    @Transactional
    override fun process(records: List<SeriesOneRecord>): List<Exercise> {
        val exercises = mutableSetOf<Exercise>()

        val series = seriesRepository.findById(1L).orElse(null)
        records.forEach {
            val answerOptions = extractAnswerOptions(it)
            resourceRepository.saveAll(answerOptions)

            val exercise = extractExercise(it, series)
            exercise.addTask(generateOneTask(exercise, answerOptions))

            exerciseRepository.save(exercise)
            exercises.add(exercise)
        }

        return exercises.toMutableList()
    }

    private fun extractAnswerOptions(record: SeriesOneRecord): MutableSet<Resource> {
        return record.words
            .asSequence()
            .map { toStringWithoutBraces(it) }
            .map { toResource(it, record.noise) }
            .toMutableSet()
    }

    private fun toResource(word: String, noise: String): Resource {
        val resource = resourceRepository.findFirstByWordLike(word)
            .orElse(
                Resource(
                    word = word,
                    audioFileUrl = "$noise/$word.mp3",
                    pictureFileUrl = pictureDefaultPath.format(word)
                )
            )
        resource.wordType = WordType.OBJECT.toString()
        return resource
    }

    private fun toStringWithoutBraces(it: String) = it.replace("[()]".toRegex(), StringUtils.EMPTY)

    private fun extractExercise(record: SeriesOneRecord, series: Series): Exercise {
        return exerciseRepository
            .findExerciseByNameAndLevel(record.exerciseName, record.level)
            .orElse(
                Exercise(
                    series = series,
                    name = record.exerciseName,
                    level = record.level,
                    exerciseType = ExerciseType.SINGLE_SIMPLE_WORDS.toString(),
                    description = record.exerciseName
                )
            )
    }

    private fun generateOneTask(exercise: Exercise, answerOptions: MutableSet<Resource>) =
        Task(exercise = exercise, serialNumber = 1, answerOptions = answerOptions)

    private fun generateTasks(exercise: Exercise, answerOptions: MutableSet<Resource>): MutableList<Task> {
        return generateCorrectAnswers(answerOptions)
            .mapIndexed { serialNumber, correctAnswer ->
                Task(
                    exercise = exercise,
                    serialNumber = serialNumber + 1,
                    answerOptions = answerOptions,
                    correctAnswer = correctAnswer
                )
            }.toMutableList()
    }

    private fun generateCorrectAnswers(answerOptions: MutableSet<Resource>): MutableList<Resource> {
        val correctAnswers = mutableListOf<Resource>()
        for (i in 1..repeatCount) {
            correctAnswers.addAll(answerOptions)
        }
        correctAnswers.shuffle(random)
        return correctAnswers
    }
}
