package com.epam.brn.upload.csv.seriesPhrases

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
import com.epam.brn.service.WordsService
import com.epam.brn.upload.csv.RecordProcessor
import com.epam.brn.upload.toStringWithoutBraces
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SeriesPhrasesRecordProcessor(
    private val subGroupRepository: SubGroupRepository,
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository,
    private val wordsService: WordsService,
) : RecordProcessor<SeriesPhrasesRecord, Exercise> {
    @Value(value = "\${fonAudioPath}")
    private lateinit var fonAudioPath: String

    override fun isApplicable(record: Any): Boolean = record is SeriesPhrasesRecord

    @Transactional
    override fun process(
        records: List<SeriesPhrasesRecord>,
        locale: BrnLocale,
    ): List<Exercise> {
        val exercises = mutableSetOf<Exercise>()

        records.forEach { record ->
            val subGroup =
                subGroupRepository.findByCodeAndLocale(record.code, locale.locale)
                    ?: throw EntityNotFoundException("No subGroup was found for code=${record.code} and locale={${locale.locale}}")
            val existExercise = exerciseRepository.findExerciseByNameAndLevel(record.exerciseName, record.level)
            if (!existExercise.isPresent) {
                val answerOptions = extractAnswerOptions(record, locale)
                resourceRepository.saveAll(answerOptions)

                val newExercise = generateExercise(record, subGroup)
                newExercise.addTask(generateOneTask(newExercise, answerOptions))

                exerciseRepository.save(newExercise)
                exercises.add(newExercise)
            }
        }

        return exercises.toMutableList()
    }

    private fun extractAnswerOptions(
        record: SeriesPhrasesRecord,
        locale: BrnLocale,
    ): MutableSet<Resource> {
        val words =
            record.phrases
                .asSequence()
                .map { it.toStringWithoutBraces() }
                .toMutableList()
        val lastWordOnFirstPhrase = words.find { w -> w.contains(".") }
        var phraseFirst =
            words
                .subList(0, words.indexOf(lastWordOnFirstPhrase) + 1)
                .joinToString(" ")
                .replace(".", "")
        var phraseSecond =
            words
                .subList(words.indexOf(lastWordOnFirstPhrase) + 1, words.size)
                .joinToString(" ")
                .replace(".", "")
        return mutableSetOf(toResource(phraseFirst, locale), toResource(phraseSecond, locale))
    }

    private fun toResource(
        phrase: String,
        locale: BrnLocale,
    ): Resource {
        val audioPath =
            wordsService.getSubFilePathForWord(
                AudioFileMetaData(
                    phrase,
                    locale.locale,
                    wordsService.getDefaultManVoiceForLocale(locale.locale),
                ),
            )
        val wordType = WordType.PHRASE.toString()
        val resource =
            resourceRepository
                .findFirstByWordAndLocaleAndWordType(phrase, locale.locale, wordType)
                .orElse(
                    Resource(
                        word = phrase,
                        locale = locale.locale,
                    ),
                )
        resource.audioFileUrl = audioPath
        resource.wordType = wordType
        return resource
    }

    private fun generateExercise(
        record: SeriesPhrasesRecord,
        subGroup: SubGroup,
    ) = Exercise(
        subGroup = subGroup,
        name = record.exerciseName,
        level = record.level,
        noiseLevel = record.noiseLevel,
        noiseUrl = if (!record.noiseUrl.isNullOrEmpty()) String.format(fonAudioPath, record.noiseUrl) else "",
        active = true,
    )

    private fun generateOneTask(
        exercise: Exercise,
        answerOptions: MutableSet<Resource>,
    ) = Task(exercise = exercise, serialNumber = 1, answerOptions = answerOptions)
}
