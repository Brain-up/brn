package com.epam.brn.upload.csv.series2

import com.epam.brn.enums.Locale
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.model.WordType
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.service.AudioFileMetaData
import com.epam.brn.service.WordsService
import com.epam.brn.upload.csv.RecordProcessor
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

    override fun isApplicable(record: Any): Boolean = record is SeriesTwoRecord

    override fun process(records: List<SeriesTwoRecord>, locale: Locale): List<Exercise> {
        val exercises = mutableSetOf<Exercise>()
        records.forEach { record ->
            val subGroup = subGroupRepository.findByCodeAndLocale(record.code, locale.locale)
                ?: throw EntityNotFoundException("No subGroup was found for code=${record.code} and locale={${locale.locale}}")
            val existExercise = exerciseRepository.findExerciseByNameAndLevel(record.exerciseName, record.level)
            if (!existExercise.isPresent) {
                val answerOptions = extractAnswerOptions(record, locale)
                wordsService.addWordsToDictionary(locale, answerOptions.map { resource -> resource.word })
                val savedResources = resourceRepository.saveAll(answerOptions)

                val newExercise = generateExercise(record, subGroup)
                val savedExercise = exerciseRepository.save(newExercise)

                taskRepository.save(extractTask(savedExercise, savedResources.toMutableSet()))
                exercises.add(savedExercise)
            }
        }
        return exercises.toMutableList()
    }

    private fun extractAnswerOptions(record: SeriesTwoRecord, locale: Locale): MutableSet<Resource> =
        extractWordGroups(record)
            .map {
                splitOnWords(it.second).map { word: String ->
                    toResource(word, it.first, locale)
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

    private fun toResource(word: String, wordType: WordType, locale: Locale): Resource {
        val audioPath = wordsService.getSubFilePathForWord(
            AudioFileMetaData(
                word,
                locale.locale,
                wordsService.getDefaultManVoiceForLocale(locale.locale)
            )
        )
        val resource = resourceRepository.findFirstByWordAndLocaleAndWordType(word, locale.locale, wordType.name)
            .orElse(
                Resource(
                    word = word,
                    pictureFileUrl = pictureWithWordFileUrl.format(word),
                    locale = locale.locale
                )
            )
        resource.audioFileUrl = audioPath
        resource.wordType = wordType.name
        return resource
    }

    private fun toStringWithoutBraces(it: String) = it.replace("[()]".toRegex(), StringUtils.EMPTY)

    private fun generateExercise(record: SeriesTwoRecord, subGroup: SubGroup) =
        Exercise(
            subGroup = subGroup,
            name = record.exerciseName,
            template = calculateTemplate(record),
            level = record.level
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
