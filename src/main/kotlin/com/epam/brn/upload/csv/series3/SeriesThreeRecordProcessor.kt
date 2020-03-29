package com.epam.brn.upload.csv.series3

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
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SeriesThreeRecordProcessor(
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository,
    private val seriesRepository: SeriesRepository
) : RecordProcessor<SeriesThreeRecord, Exercise> {

    @Value(value = "\${brn.audio.file.second.series.path}")
    private lateinit var audioFileUrl: String

    @Value(value = "\${brn.picture.file.default.path}")
    private lateinit var pictureFileUrl: String

    override fun isApplicable(record: Any): Boolean {
        return record is SeriesThreeRecord
    }

    @Transactional
    override fun process(records: List<SeriesThreeRecord>): List<Exercise> {
        val exercises = mutableSetOf<Exercise>()

        val series = seriesRepository.findById(3L).orElse(null)
        records.forEach {
            val correctAnswer = extractCorrectAnswer(it)
            resourceRepository.save(correctAnswer)

            val answerOptions = extractAnswerOptions(it)
            resourceRepository.saveAll(answerOptions)

            val exercise = extractExercise(it, series)
            exercise.addTask(createTask(exercise, correctAnswer, answerOptions))

            exerciseRepository.save(exercise)
            exercises.add(exercise)
        }

        return exercises.toMutableList()
    }

    private fun extractCorrectAnswer(record: SeriesThreeRecord): Resource {
        val answerWord = toStringWithoutBraces(record.answerParts)
        return resourceRepository
            .findFirstByWordAndAudioFileUrlLike(answerWord, record.answerAudioFile)
            .orElse(
                Resource(
                    word = answerWord,
                    wordType = WordType.SENTENCE.toString(),
                    audioFileUrl = record.answerAudioFile
                )
            )
    }

    private fun toStringWithoutBraces(it: String) = it.replace("[()]".toRegex(), StringUtils.EMPTY)

    private fun extractAnswerOptions(record: SeriesThreeRecord): MutableSet<Resource> {
        return extractWordGroups(record)
            .map {
                splitOnWords(it.second).map { word: String ->
                    toResource(word, it.first)
                }
            }
            .flatten().toMutableSet()
    }

    private fun extractWordGroups(record: SeriesThreeRecord): Sequence<Pair<WordType, String>> {
        return record.words
            .asSequence()
            .map { toStringWithoutBraces(it) }
            .mapIndexed { wordGroupPosition, wordGroup ->
                WordType.of(wordGroupPosition) to wordGroup
            }
            .filter { StringUtils.isNotBlank(it.second) }
    }

    private fun toResource(word: String, wordType: WordType): Resource {
        return resourceRepository
            .findFirstByWordLike(word)
            .orElse(
                Resource(
                    word = word,
                    wordType = wordType.toString(),
                    audioFileUrl = audioFileUrl,
                    pictureFileUrl = pictureFileUrl
                )
            )
    }

    private fun extractExercise(record: SeriesThreeRecord, series: Series): Exercise {
        return exerciseRepository.findExerciseByNameAndLevel(record.exerciseName, record.level)
            .orElse(
                Exercise(
                    series = series,
                    name = record.exerciseName,
                    description = record.exerciseName,
                    template = calculateTemplate(record),
                    exerciseType = ExerciseType.SENTENCE.toString(),
                    level = record.level
                )
            )
    }

    private fun calculateTemplate(record: SeriesThreeRecord): String {
        return extractWordGroups(record)
            .joinToString(StringUtils.SPACE, "<", ">") { it.first.toString() }
    }

    private fun createTask(
        exercise: Exercise,
        correctAnswer: Resource,
        answerOptions: MutableSet<Resource>
    ): Task {
        return Task(
            serialNumber = 2,
            answerOptions = answerOptions,
            correctAnswer = correctAnswer,
            answerParts = extractAnswerParts(correctAnswer, answerOptions),
            exercise = exercise
        )
    }

    private fun extractAnswerParts(
        correctAnswer: Resource,
        answerOptions: MutableSet<Resource>
    ): MutableMap<Int, Resource> {

        return splitOnWords(correctAnswer.word!!)
            .map { toFirstEqualResource(answerOptions, it) }
            .mapIndexed { index, resource -> index + 1 to resource }
            .toMap(mutableMapOf())
    }

    private fun splitOnWords(sentence: String): List<String> {
        return sentence
            .split(' ')
            .map { it.trim() }
    }

    private fun toFirstEqualResource(set: MutableSet<Resource>, word: String): Resource {
        return set.first { it.word.equals(word) }
    }
}
