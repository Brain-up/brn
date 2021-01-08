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
import com.epam.brn.service.WordsService
import com.epam.brn.upload.csv.RecordProcessor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SeriesThreeRecordProcessor(
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository,
    private val seriesRepository: SeriesRepository,
    private val wordsService: WordsService
) : RecordProcessor<SeriesThreeRecord, Exercise> {

    @Value(value = "\${brn.pictureWithWord.file.default.path}")
    private lateinit var pictureWithWordFileUrl: String

    @Value(value = "\${series3WordsFileName}")
    private lateinit var series3WordsFileName: String

    @Value(value = "\${audioPath}")
    private lateinit var audioPath: String

    val words = mutableMapOf<String, String>()

    override fun isApplicable(record: Any): Boolean {
        return record is SeriesThreeRecord
    }

    @Transactional
    override fun process(records: List<SeriesThreeRecord>): List<Exercise> {
        val exercises = mutableSetOf<Exercise>()

        val series = seriesRepository.findById(3L).orElse(null)
        records.forEach {
            val correctAnswer = extractCorrectAnswer(it)
            words[correctAnswer.word] = DigestUtils.md5Hex(correctAnswer.word)
            resourceRepository.save(correctAnswer)

            val answerOptions = extractAnswerOptions(it)
            words.putAll(answerOptions.associate { r -> Pair(r.word, DigestUtils.md5Hex(r.word)) })
            resourceRepository.saveAll(answerOptions)

            val exercise = extractExercise(it, series)
            exercise.addTask(createTask(exercise, correctAnswer, answerOptions))

            exerciseRepository.save(exercise)
            exercises.add(exercise)
        }
        wordsService.createTxtFileWithExerciseWordsMap(words, series3WordsFileName)
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
        val hashWord = DigestUtils.md5Hex(word)
        val audioFileUrl = audioPath.format(hashWord)
        return resourceRepository
            .findFirstByWordLike(word)
            .orElse(
                Resource(
                    word = word,
                    wordType = wordType.toString(),
                    audioFileUrl = audioFileUrl,
                    pictureFileUrl = pictureWithWordFileUrl
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

        return splitOnWords(correctAnswer.word)
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
