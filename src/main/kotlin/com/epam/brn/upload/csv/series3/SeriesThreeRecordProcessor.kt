package com.epam.brn.upload.csv.series3

import com.epam.brn.integration.repo.ExerciseRepository
import com.epam.brn.integration.repo.ResourceRepository
import com.epam.brn.integration.repo.SubGroupRepository
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.model.WordType
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
    private val subGroupRepository: SubGroupRepository,
    private val wordsService: WordsService
) : RecordProcessor<SeriesThreeRecord, Exercise> {

    @Value(value = "\${brn.pictureWithWord.file.default.path}")
    private lateinit var pictureWithWordFileUrl: String

    @Value(value = "\${series3WordsFileName}")
    private lateinit var series3WordsFileName: String

    @Value(value = "\${audioPath}")
    private lateinit var audioPath: String

    override fun isApplicable(record: Any): Boolean {
        return record is SeriesThreeRecord
    }

    @Transactional
    override fun process(records: List<SeriesThreeRecord>): List<Exercise> {
        val words = mutableSetOf<String>()
        val exercises = mutableSetOf<Exercise>()

        records.forEach {
            val correctAnswer = extractCorrectAnswer(it)
            words.add(correctAnswer.word)
            resourceRepository.save(correctAnswer)

            val answerOptions = extractAnswerOptions(it)
            words.addAll(answerOptions.map { r -> r.word }.toSet())
            resourceRepository.saveAll(answerOptions)

            val subGroup = subGroupRepository.findByCode(it.code)
            val exercise = extractExercise(it, subGroup)
            exercise.addTask(createTask(exercise, correctAnswer, answerOptions))

            exerciseRepository.save(exercise)
            exercises.add(exercise)
        }
        wordsService.createTxtFileWithExerciseWords(words, series3WordsFileName)
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

    private fun extractExercise(record: SeriesThreeRecord, subGroup: SubGroup): Exercise {
        return exerciseRepository.findExerciseByNameAndLevel(record.exerciseName, record.level)
            .orElse(
                Exercise(
                    subGroup = subGroup,
                    name = record.exerciseName,
                    template = calculateTemplate(record),
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
