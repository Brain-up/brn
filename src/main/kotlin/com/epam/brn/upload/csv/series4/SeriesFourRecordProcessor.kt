package com.epam.brn.upload.csv.series4

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Resource
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.model.WordType
import com.epam.brn.integration.repo.ExerciseRepository
import com.epam.brn.integration.repo.ResourceRepository
import com.epam.brn.integration.repo.SubGroupRepository
import com.epam.brn.service.WordsService
import com.epam.brn.upload.csv.RecordProcessor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.Random

@Component
class SeriesFourRecordProcessor(
    private val subGroupRepository: SubGroupRepository,
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository,
    private val wordsService: WordsService
) : RecordProcessor<SeriesFourRecord, Exercise> {

    @Value(value = "\${brn.picture.file.default.path}")
    private lateinit var pictureDefaultPath: String

    @Value(value = "\${brn.picture.theme.path}")
    private lateinit var pictureTheme: String

    @Value(value = "\${series4WordsFileName}")
    private lateinit var series4WordsFileName: String

    @Value(value = "\${audioPath}")
    private lateinit var audioPathFilipp: String

    @Value(value = "\${audioPathAlena}")
    private lateinit var audioPathAlena: String

    @Value(value = "\${fonAudioPath}")
    private lateinit var fonAudioPath: String

    val words = mutableMapOf<String, String>()

    private val repeatCount = 2

    var random = Random()

    override fun isApplicable(record: Any): Boolean {
        return record is SeriesFourRecord
    }

    @Transactional
    override fun process(records: List<SeriesFourRecord>): List<Exercise> {
        val exercises = mutableSetOf<Exercise>()

        records.forEach {
            val answerOptions = extractAnswerOptions(it)
            resourceRepository.saveAll(answerOptions)

            val subGroup = subGroupRepository.findByCode(it.code)
            val exercise = extractExercise(it, subGroup)
            exercise.addTask(generateOneTask(exercise, answerOptions))

            exerciseRepository.save(exercise)
            exercises.add(exercise)
        }
        wordsService.createTxtFileWithExerciseWordsMap(words, series4WordsFileName)
        return exercises.toMutableList()
    }

    private fun extractAnswerOptions(record: SeriesFourRecord): MutableSet<Resource> {
        var audioPath = audioPathFilipp
        val words = record.phrases
            .asSequence()
            .map { toPhrasesWithoutBraces(it) }
            .toMutableList()
        val lastWordOnFirstPhrase = words.find { w -> w.contains(".") }
        var phraseFirst = words.subList(0, words.indexOf(lastWordOnFirstPhrase) + 1)
            .joinToString(" ").replace(".", "")
        var phraseSecond = words.subList(words.indexOf(lastWordOnFirstPhrase) + 1, words.size)
            .joinToString(" ").replace(".", "")
        return mutableSetOf(toResource(phraseFirst, audioPath), toResource(phraseSecond, audioPath))
    }

    private fun toResource(phrase: String, audioPath: String): Resource {
        val phraseHex = DigestUtils.md5Hex(phrase)
        words.put(phrase, phraseHex)
        val audioFileUrl = audioPath.format(phraseHex)
        val resource = resourceRepository.findFirstByWordAndAudioFileUrlLike(phrase, audioFileUrl)
            .orElse(
                Resource(
                    word = phrase,
                    audioFileUrl = audioFileUrl
                )
            )
        resource.wordType = WordType.OBJECT.toString()
        return resource
    }

    private fun toPhrasesWithoutBraces(it: String) = it.replace("[()]".toRegex(), StringUtils.EMPTY)

    private fun extractExercise(record: SeriesFourRecord, subGroup: SubGroup): Exercise {
        return exerciseRepository
            .findExerciseByNameAndLevel(record.exerciseName, record.level)
            .orElse(
                Exercise(
                    subGroup = subGroup,
                    name = record.exerciseName,
                    pictureUrl = if (!record.code.isNullOrEmpty()) String.format(pictureTheme, record.code) else "",
                    level = record.level,
                    noiseLevel = record.noiseLevel,
                    noiseUrl = if (!record.noiseUrl.isNullOrEmpty()) String.format(fonAudioPath, record.noiseUrl) else "",
                    exerciseType = ExerciseType.PHRASES.toString(),
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
