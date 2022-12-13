package com.epam.brn.upload.csv.seriesMatrix

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.WordType
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.service.WordsService
import com.epam.brn.upload.csv.RecordProcessor
import com.epam.brn.upload.toStringWithoutBraces
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class SeriesMatrixRecordProcessor(
    private val subGroupRepository: SubGroupRepository,
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository,
    private val taskRepository: TaskRepository,
    private val wordsService: WordsService
) : RecordProcessor<SeriesMatrixRecord, Exercise> {

    override fun isApplicable(record: Any): Boolean = record is SeriesMatrixRecord

    @Transactional
    override fun process(records: List<SeriesMatrixRecord>, locale: BrnLocale): List<Exercise> {
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

    private fun extractAnswerOptions(record: SeriesMatrixRecord, locale: BrnLocale): MutableSet<Resource> =
        extractWordGroups(record)
            .map {
                splitOnWords(it.second).map { word: String ->
                    toResource(word, it.first, locale)
                }
            }
            .flatten().toMutableSet()

    private fun extractWordGroups(record: SeriesMatrixRecord): Sequence<Pair<WordType, String>> =
        record.words
            .asSequence()
            .map { it.toStringWithoutBraces() }
            .mapIndexed { wordGroupPosition, wordGroup ->
                WordType.of(wordGroupPosition) to wordGroup
            }
            .filter { StringUtils.isNotBlank(it.second) }

    private fun splitOnWords(sentence: String): List<String> = sentence.split(' ').map { it.trim() }

    private fun toResource(word: String, wordType: WordType, locale: BrnLocale): Resource {
        val audioPath = wordsService.getSubFilePathForWord(
            AudioFileMetaData(
                word,
                locale.locale,
                wordsService.getDefaultManVoiceForLocale(locale.locale)
            )
        )
        val resource = resourceRepository.findFirstByWordAndLocaleAndWordType(word, locale.locale, wordType.name)
            .orElse(Resource(word = word, locale = locale.locale))
        resource.audioFileUrl = audioPath
        resource.wordType = wordType.name
        return resource
    }

    private fun generateExercise(record: SeriesMatrixRecord, subGroup: SubGroup) =
        Exercise(
            subGroup = subGroup,
            name = record.exerciseName,
            template = calculateTemplate(record),
            level = record.level
        )

    private fun calculateTemplate(record: SeriesMatrixRecord): String =
        extractWordGroups(record)
            .joinToString(StringUtils.SPACE, "<", ">") { it.first.toString() }

    private fun extractTask(exercise: Exercise, answerOptions: MutableSet<Resource>): Task =
        Task(
            serialNumber = 2,
            answerOptions = answerOptions,
            exercise = exercise
        )
}
