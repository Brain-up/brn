package com.epam.brn.upload.csv.series2

import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.SubGroup
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

@Component
class SeriesTwoRecordProcessor(
    private val subGroupRepository: SubGroupRepository,
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository,
    private val taskRepository: TaskRepository,
    private val wordsService: WordsService
) : RecordProcessor<SeriesTwoRecord, Exercise> {

    @Value(value = "\${brn.pictureWithWord.file.default.path}")
    private lateinit var pictureWithWordFileUrl: String

    @Value(value = "\${series2WordsFileName}")
    private lateinit var series2WordsFileName: String

    @Value(value = "\${audioPath}")
    private lateinit var audioPath: String

    val words = mutableMapOf<String, String>()

    override fun isApplicable(record: Any): Boolean = record is SeriesTwoRecord

    override fun process(records: List<SeriesTwoRecord>): List<Exercise> {
        val exercises = mutableSetOf<Exercise>()

        records.forEach {
            val answerOptions = extractAnswerOptions(it)
            words.putAll(answerOptions.associate { r -> Pair(r.word, DigestUtils.md5Hex(r.word)) })
            val savedResources = resourceRepository.saveAll(answerOptions)

            val subGroup = subGroupRepository.findByCode(it.code)
            val exercise = extractExercise(it, subGroup)
            val savedExercise = exerciseRepository.save(exercise)

            taskRepository.save(extractTask(savedExercise, savedResources.toMutableSet()))
            exercises.add(savedExercise)
        }
        wordsService.createTxtFileWithExerciseWordsMap(words, series2WordsFileName)
        return exercises.toMutableList()
    }

    private fun extractAnswerOptions(record: SeriesTwoRecord): MutableSet<Resource> =
        extractWordGroups(record)
            .map {
                splitOnWords(it.second).map { word: String ->
                    toResource(word, it.first)
                }
            }
            .flatten().toMutableSet()

    private fun extractWordGroups(record: SeriesTwoRecord): Sequence<Pair<WordType, String>> =
        record.words
            .asSequence()
            .map { toStringWithoutBraces(it) }
            .mapIndexed { wordGroupPosition, wordGroup ->
                WordType.of(wordGroupPosition) to wordGroup
            }
            .filter { StringUtils.isNotBlank(it.second) }

    private fun splitOnWords(sentence: String): List<String> = sentence.split(' ').map { it.trim() }

    private fun toResource(word: String, wordType: WordType): Resource {
        val hashWord = DigestUtils.md5Hex(word)
        words[word] = hashWord
        val audioFileUrl = audioPath.format(hashWord)
        val resource = resourceRepository.findFirstByWordAndWordType(word, wordType.name)
            .orElse(
                Resource(
                    word = word,
                    audioFileUrl = audioFileUrl,
                    pictureFileUrl = pictureWithWordFileUrl.format(word)
                )
            )
        resource.wordType = wordType.name
        return resource
    }

    private fun toStringWithoutBraces(it: String) = it.replace("[()]".toRegex(), StringUtils.EMPTY)

    private fun extractExercise(record: SeriesTwoRecord, subGroup: SubGroup): Exercise =
        exerciseRepository
            .findExerciseByNameAndLevel(record.exerciseName, record.level)
            .orElse(
                Exercise(
                    subGroup = subGroup,
                    name = record.exerciseName,
                    template = calculateTemplate(record),
                    level = record.level
                )
            )

    private fun calculateTemplate(record: SeriesTwoRecord): String =
        extractWordGroups(record)
            .joinToString(StringUtils.SPACE, "<", ">") { it.first.toString() }

    private fun extractTask(exercise: Exercise, answerOptions: MutableSet<Resource>): Task {
        return Task(
            serialNumber = 2,
            answerOptions = answerOptions,
            exercise = exercise
        )
    }
}
