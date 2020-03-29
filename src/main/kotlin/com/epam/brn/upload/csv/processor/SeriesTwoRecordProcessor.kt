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
import com.epam.brn.upload.csv.record.SeriesTwoRecord
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SeriesTwoRecordProcessor(
    private val seriesRepository: SeriesRepository,
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository
) {

    @Value(value = "\${brn.audio.file.second.series.path}")
    private lateinit var audioFileUrl: String

    @Value(value = "\${brn.picture.file.default.path}")
    private lateinit var pictureFileUrl: String

    fun process(records: List<SeriesTwoRecord>): List<Exercise> {
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

    private fun extractAnswerOptions(record: SeriesTwoRecord): MutableSet<Resource> {
        return extractWordGroups(record)
            .map {
                splitOnWords(it.second).map { word: String ->
                    toResource(word, it.first)
                }
            }
            .flatten().toMutableSet()
    }

    private fun extractWordGroups(record: SeriesTwoRecord): Sequence<Pair<WordType, String>> {
        return record.words
            .asSequence()
            .map { toStringWithoutBraces(it) }
            .mapIndexed { wordGroupPosition, wordGroup ->
                WordType.of(wordGroupPosition) to wordGroup
            }
            .filter { StringUtils.isNotBlank(it.second) }
    }

    private fun splitOnWords(sentence: String): List<String> = sentence.split(' ').map { it.trim() }

    private fun toResource(word: String, wordType: WordType): Resource {
        val resource = resourceRepository.findFirstByWordLike(word)
            .orElse(
                Resource(
                    word = word,
                    audioFileUrl = audioFileUrl.format(word),
                    pictureFileUrl = pictureFileUrl.format(word)
                )
            )
        resource.wordType = wordType.toString()
        return resource
    }

    private fun toStringWithoutBraces(it: String) = it.replace("[()]".toRegex(), StringUtils.EMPTY)

    private fun extractExercise(record: SeriesTwoRecord, series: Series): Exercise {
        return exerciseRepository
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
    }

    private fun calculateTemplate(record: SeriesTwoRecord): String {
        return extractWordGroups(record)
            .joinToString(StringUtils.SPACE, "<", ">") { it.first.toString() }
    }

    private fun extractTask(exercise: Exercise, answerOptions: MutableSet<Resource>): Task {
        return Task(
            serialNumber = 2,
            answerOptions = answerOptions,
            exercise = exercise
        )
    }
}
