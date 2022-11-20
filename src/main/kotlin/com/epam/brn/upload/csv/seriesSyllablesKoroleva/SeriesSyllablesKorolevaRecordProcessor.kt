package com.epam.brn.upload.csv.seriesSyllablesKoroleva

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
import com.epam.brn.upload.csv.RecordProcessor
import com.epam.brn.upload.toStringWithoutBraces
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SeriesSyllablesKorolevaRecordProcessor(
    private val subGroupRepository: SubGroupRepository,
    private val resourceRepository: ResourceRepository,
    private val exerciseRepository: ExerciseRepository,
) : RecordProcessor<SeriesSyllablesKorolevaRecord, Exercise> {

    override fun isApplicable(record: Any): Boolean = record is SeriesSyllablesKorolevaRecord

    @Transactional
    override fun process(records: List<SeriesSyllablesKorolevaRecord>, locale: BrnLocale): List<Exercise> {
        val exercises = mutableSetOf<Exercise>()
        records.forEach { record ->
            val subGroup = subGroupRepository.findByCodeAndLocale(record.code, locale.locale)
                ?: throw EntityNotFoundException("No subGroup was found for code=${record.code} and locale={${locale.locale}}")
            val existExercise = exerciseRepository.findExerciseByNameAndLevel(record.exerciseName, record.level)
            if (!existExercise.isPresent) {
                val answerOptions = extractAnswerOptions(record, locale)
                resourceRepository.saveAll(answerOptions)
                val newExercise = generateExercise(record, subGroup)
                val newTask = Task(exercise = newExercise, answerOptions = answerOptions)
                newExercise.addTask(newTask)
                exerciseRepository.save(newExercise)
                exercises.add(newExercise)
            }
        }
        return exercises.toMutableList()
    }

    private fun extractAnswerOptions(record: SeriesSyllablesKorolevaRecord, locale: BrnLocale): MutableList<Resource> =
        record.words
            .asSequence()
            .map { it.toStringWithoutBraces() }
            .map { toResource(it, locale) }
            .toMutableList()

    private fun toResource(word: String, locale: BrnLocale): Resource {
        val resource =
            resourceRepository.findFirstByWordAndLocaleAndWordType(word, locale.locale, WordType.OBJECT.toString())
                .orElse(
                    Resource(
                        word = word,
                        locale = locale.locale,
                    )
                )
        resource.wordType = WordType.OBJECT.toString()
        return resource
    }

    private fun generateExercise(record: SeriesSyllablesKorolevaRecord, subGroup: SubGroup) =
        Exercise(
            subGroup = subGroup,
            name = record.exerciseName,
            level = record.level,
            wordsColumns = record.wordsColumns,
        )
}
