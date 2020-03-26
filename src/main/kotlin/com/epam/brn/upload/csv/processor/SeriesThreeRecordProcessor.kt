package com.epam.brn.upload.csv.processor

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.upload.csv.record.SeriesThreeRecord
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SeriesThreeRecordProcessor(
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository,
    private val seriesRepository: SeriesRepository
) {

    @Value(value = "\${brn.audio.file.second.series.path}")
    private lateinit var audioFileUrl: String

    @Value(value = "\${brn.picture.file.default.path}")
    private lateinit var pictureFileUrl: String

    @Transactional
    fun process(records: List<SeriesThreeRecord>): List<Exercise> {
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
                    wordType = WordTypeEnum.SENTENCE.toString(),
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

    private fun extractWordGroups(record: SeriesThreeRecord): Sequence<Pair<WordTypeEnum, String>> {
        return record.words
            .asSequence()
            .map { toStringWithoutBraces(it) }
            .mapIndexed { wordGroupPosition, wordGroup ->
                calcWordTypeByWordGroupPosition(wordGroupPosition) to wordGroup
            }
            .filter { StringUtils.isNotBlank(it.second) }
    }

    private fun toResource(word: String, wordType: WordTypeEnum): Resource {
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

    private fun calcWordTypeByWordGroupPosition(wordPositionNumber: Int): WordTypeEnum {
        return when (wordPositionNumber) {
            0 -> WordTypeEnum.COUNT
            1 -> WordTypeEnum.OBJECT_DESCRIPTION
            2 -> WordTypeEnum.OBJECT
            3 -> WordTypeEnum.OBJECT_ACTION
            4 -> WordTypeEnum.ADDITION_OBJECT_DESCRIPTION
            5 -> WordTypeEnum.ADDITION_OBJECT
            else -> WordTypeEnum.UNKNOWN
        }
    }

    private fun extractExercise(record: SeriesThreeRecord, series: Series): Exercise {
        return exerciseRepository.findExerciseByNameAndLevel(record.exerciseName, record.level)
            .orElse(
                Exercise(
                    series = series,
                    name = record.exerciseName,
                    description = record.exerciseName,
                    template = calculateTemplate(record),
                    exerciseType = ExerciseTypeEnum.SENTENCE.toString(),
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
