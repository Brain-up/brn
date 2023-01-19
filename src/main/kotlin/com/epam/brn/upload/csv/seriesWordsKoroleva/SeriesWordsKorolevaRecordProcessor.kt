package com.epam.brn.upload.csv.seriesWordsKoroleva

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.BrnLocale
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.enums.WordType
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.service.WordsService
import com.epam.brn.upload.csv.RecordProcessor
import com.epam.brn.upload.toStringWithoutBraces
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SeriesWordsKorolevaRecordProcessor(
    private val subGroupRepository: SubGroupRepository,
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository,
    private val wordsService: WordsService
) : RecordProcessor<SeriesWordsKorolevaRecord, Exercise> {

    override fun isApplicable(record: Any): Boolean = record is SeriesWordsKorolevaRecord

    @Transactional
    override fun process(records: List<SeriesWordsKorolevaRecord>, locale: BrnLocale): List<Exercise> {
        val exercises = mutableSetOf<Exercise>()
        records.forEach { record ->
            val subGroup = subGroupRepository.findByCodeAndLocale(record.code, locale.locale)
                ?: throw EntityNotFoundException("No subGroup was found for code=${record.code} and locale={${locale.locale}}")
            val existExercise = exerciseRepository.findExerciseByNameAndLevel(record.exerciseName, record.level)
            if (!existExercise.isPresent) {
                val answerOptions = extractAnswerOptions(record, locale)
                wordsService.addWordsToDictionary(locale, answerOptions.map { resource -> resource.word })
                resourceRepository.saveAll(answerOptions)

                val newExercise = generateExercise(record, subGroup)
                newExercise.addTask(generateOneTask(newExercise, answerOptions))
                exerciseRepository.save(newExercise)
                exercises.add(newExercise)
            }
        }
        return exercises.toMutableList()
    }

    private fun extractAnswerOptions(record: SeriesWordsKorolevaRecord, locale: BrnLocale): MutableSet<Resource> =
        record.words
            .asSequence()
            .map { it.toStringWithoutBraces() }
            .map { toResource(it, locale) }
            .toMutableSet()

    private fun toResource(word: String, locale: BrnLocale): Resource {
        val audioPath = wordsService.getSubFilePathForWord(
            AudioFileMetaData(
                word,
                locale.locale,
                wordsService.getDefaultWomanVoiceForLocale(locale.locale)
            )
        )
        val resource =
            resourceRepository.findFirstByWordAndLocaleAndWordType(word, locale.locale, WordType.OBJECT.toString())
                .orElse(
                    Resource(
                        word = word,
                        locale = locale.locale,
                    )
                )
        resource.audioFileUrl = audioPath
        resource.wordType = WordType.OBJECT.toString()
        return resource
    }

    private fun generateExercise(record: SeriesWordsKorolevaRecord, subGroup: SubGroup) =
        Exercise(
            subGroup = subGroup,
            name = record.exerciseName,
            level = record.level,
            playWordsCount = record.playWordsCount,
            wordsColumns = record.wordsColumns,
        )

    private fun generateOneTask(exercise: Exercise, answerOptions: MutableSet<Resource>) =
        Task(exercise = exercise, answerOptions = answerOptions)
}
