package com.epam.brn.upload.csv.series2

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
class SeriesTwoRecordProcessor(
    private val seriesRepository: SeriesRepository,
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository
) : RecordProcessor<SeriesTwoRecord, Exercise> {

    @Value(value = "\${brn.audio.file.second.series.path}")
    private lateinit var audioFileUrl: String

    @Value(value = "\${brn.pictureWithWord.file.default.path}")
    private lateinit var pictureWithWordFileUrl: String

    override fun isApplicable(record: Any): Boolean = record is SeriesTwoRecord

    @Transactional
    override fun process(records: List<SeriesTwoRecord>): List<Exercise> {
        val exercises = mutableSetOf<Exercise>()

        val series = seriesRepository.findById(2L).orElse(null)
        records.forEach {
            val answerOptions = extractAnswerOptions(it)
            resourceRepository.saveAll(answerOptions)

            val exercise = extractExercise(it, series)
            exercise.addTask(extractTask(exercise, answerOptions))

            exerciseRepository.save(exercise)
            exercises.add(exercise)
        }

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
        val resource = resourceRepository.findFirstByWordLike(word)
            .orElse(
                Resource(
                    word = word,
                    audioFileUrl = audioFileUrl.format(word),
                    pictureFileUrl = pictureWithWordFileUrl.format(word)
                )
            )
        resource.wordType = wordType.toString()
        return resource
    }

    private fun toStringWithoutBraces(it: String) = it.replace("[()]".toRegex(), StringUtils.EMPTY)

    private fun extractExercise(record: SeriesTwoRecord, series: Series): Exercise =
        exerciseRepository
            .findExerciseByNameAndLevel(record.exerciseName, record.level)
            .orElse(
                Exercise(
                    series = series,
                    name = record.exerciseName,
                    description = record.exerciseName,
                    template = calculateTemplate(record),
                    exerciseType = ExerciseType.WORDS_SEQUENCES.toString(),
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
