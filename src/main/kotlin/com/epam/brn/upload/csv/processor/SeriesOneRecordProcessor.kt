package com.epam.brn.upload.csv.processor

import com.epam.brn.constant.ExerciseType
import com.epam.brn.constant.WordType
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.upload.csv.record.SeriesOneRecord
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SeriesOneRecordProcessor(
    var seriesRepository: SeriesRepository,
    var resourceRepository: ResourceRepository,
    var exerciseRepository: ExerciseRepository
) {

    @Value(value = "\${brn.audio.file.default.path}")
    private lateinit var defaultAudioFileUrl: String

    fun process(records: List<SeriesOneRecord>): List<Exercise> {
        val exercises = mutableSetOf<Exercise>()

        val series = seriesRepository.findById(1L).orElse(null)
        records.forEach {

            val correctAnswer = extractCorrectAnswer(it)
            resourceRepository.save(correctAnswer)

            val answerOptions = extractAnswerOptions(it)
            resourceRepository.saveAll(answerOptions)

            val exercise = extractExercise(it, series)
            exercise.addTask(extractTask(it, exercise, correctAnswer, answerOptions))

            exerciseRepository.save(exercise)
            exercises.add(exercise)
        }

        return exercises.toList()
    }

    private fun extractCorrectAnswer(record: SeriesOneRecord): Resource {
        val resource = resourceRepository
            .findFirstByWordAndAudioFileUrlLike(record.word, record.audioFileName)
            .orElse(
                Resource(
                    audioFileUrl = record.audioFileName,
                    word = record.word
                )
            )

        resource.wordType = record.wordType
        resource.pictureFileUrl = record.pictureFileName

        return resource
    }

    private fun extractAnswerOptions(record: SeriesOneRecord): MutableSet<Resource> {
        return CollectionUtils.emptyIfNull(record.words)
            .asSequence()
            .map { toStringWithoutBraces(it) }
            .filter { StringUtils.isNotEmpty(it) }
            .map { toResource(it) }
            .toMutableSet()
    }

    private fun toStringWithoutBraces(it: String) = it.replace("[()]".toRegex(), StringUtils.EMPTY)

    private fun toResource(word: String): Resource {
        return resourceRepository
            .findFirstByWordLike(word)
            .orElse(
                Resource(
                    audioFileUrl = defaultAudioFileUrl.format(word),
                    word = word,
                    wordType = WordType.UNKNOWN.toString(),
                    pictureFileUrl = null
                )
            )
    }

    private fun extractExercise(record: SeriesOneRecord, series: Series): Exercise {
        return exerciseRepository
            .findExerciseByNameAndLevel(record.exerciseName, record.level)
            .orElse(
                Exercise(
                    name = record.exerciseName,
                    level = record.level,
                    series = series,
                    exerciseType = ExerciseType.SINGLE_WORDS.toString()
                )
            )
    }

    private fun extractTask(
        record: SeriesOneRecord,
        exercise: Exercise,
        correctAnswer: Resource,
        answerOptions: MutableSet<Resource>
    ): Task {
        return Task(
            serialNumber = record.orderNumber,
            exercise = exercise,
            correctAnswer = correctAnswer,
            answerOptions = answerOptions
        )
    }
}
