package com.epam.brn.upload.csv.series1

import com.epam.brn.model.Exercise
import com.epam.brn.enums.ExerciseType
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.enums.WordType
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
import java.util.Random

@Component
class SeriesOneRecordProcessor(
    private val seriesRepository: SeriesRepository,
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository,
    private val wordsService: WordsService
) : RecordProcessor<SeriesOneRecord, Exercise> {

    @Value(value = "\${brn.picture.file.default.path}")
    private lateinit var pictureDefaultPath: String

    @Value(value = "\${brn.picture.theme.path}")
    private lateinit var pictureTheme: String

    @Value(value = "\${series1WordsFileName}")
    private lateinit var series1WordsFileName: String

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
        wordsService.createTxtFileWithExerciseWordsMap(words, series1WordsFileName)
        return exercises.toMutableList()
    }

    private fun extractAnswerOptions(record: SeriesOneRecord): MutableSet<Resource> {
        var audioPath = audioPathFilipp
        if (record.exerciseName.startsWith("М"))
            audioPath = audioPathAlena
        return record.words
            .asSequence()
            .map { toStringWithoutBraces(it) }
            .map { toResource(it, audioPath) }
            .toMutableSet()
    }

    private fun toResource(word: String, audioPath: String): Resource {
        val hashWord = DigestUtils.md5Hex(word)
        words[word] = hashWord
        val audioFileUrl = audioPath.format(hashWord)
        val resource = resourceRepository.findFirstByWordAndAudioFileUrlLike(word, audioFileUrl)
            .orElse(
                Resource(
                    word = word,
                    audioFileUrl = audioFileUrl,
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
                    pictureUrl = if (!record.pictureUrl.isNullOrEmpty()) String.format(pictureTheme, record.pictureUrl) else "",
                    level = record.level,
                    noiseLevel = record.noiseLevel,
                    noiseUrl = if (!record.noiseUrl.isNullOrEmpty()) String.format(fonAudioPath, record.noiseUrl) else "",
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
