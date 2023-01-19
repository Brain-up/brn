package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.request.exercise.ExerciseCreateDto
import com.epam.brn.dto.request.exercise.ExercisePhrasesCreateDto
import com.epam.brn.dto.request.exercise.ExerciseSentencesCreateDto
import com.epam.brn.dto.request.exercise.ExerciseWordsCreateDto
import com.epam.brn.dto.response.ExerciseWithWordsResponse
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.BrnRole
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.upload.csv.RecordProcessor
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
    private val urlConversionService: UrlConversionService,
    private val recordProcessors: List<RecordProcessor<out Any, out Any>>,
) {

    @Value(value = "\${minRepetitionIndex}")
    private lateinit var minRepetitionIndex: Number

    @Value(value = "\${minRightAnswersIndex}")
    private lateinit var minRightAnswersIndex: Number

    private val log = logger()

    fun findExerciseById(exerciseID: Long): ExerciseDto {
        val exercise = exerciseRepository.findById(exerciseID)
            .orElseThrow { EntityNotFoundException("Could not find requested exerciseID=$exerciseID") }
        return updateExerciseDto(exercise.toDto())
    }

    fun findExerciseByNameAndLevel(name: String, level: Int): Exercise =
        exerciseRepository
            .findExerciseByNameAndLevel(name, level)
            .orElseThrow { EntityNotFoundException("Exercise was not found by name=$name and level=$level") }

    fun findExercisesByUserId(userId: Long): List<ExerciseDto> {
        log.info("Searching available exercises for user=$userId")
        val exercisesIdList = studyHistoryRepository.getDoneExercisesIdList(userId)
        val exercises = exerciseRepository.findAll()
        return exercises.map { exercise ->
            updateExerciseDto(exercise.toDto(exercisesIdList.contains(exercise.id)))
        }
    }

    fun findExercisesBySubGroupForCurrentUser(subGroupId: Long): List<ExerciseDto> {
        val currentUserId = userAccountService.getCurrentUserId()
        return findExercisesByUserIdAndSubGroupId(currentUserId, subGroupId)
    }

    fun findExercisesByUserIdAndSubGroupId(userId: Long, subGroupId: Long): List<ExerciseDto> {
        log.info("Searching exercises for user=$userId with subGroupId=$subGroupId with Availability")
        val subGroupExercises = exerciseRepository.findExercisesBySubGroupId(subGroupId).sortedBy { s -> s.level }
        val currentUserRoles = userAccountService.getCurrentUserRoles()
        log.info("Current user is admin: ${userId == 1L}))")
        if (currentUserRoles.contains(BrnRole.ADMIN) || currentUserRoles.contains(BrnRole.SPECIALIST))
            return subGroupExercises.map { exercise -> updateExerciseDto(exercise.toDto(true)) }
        val doneSubGroupExercises = studyHistoryRepository.getDoneExercises(subGroupId, userId)
        val openSubGroupExercises =
            getAvailableExercisesForSubGroup(doneSubGroupExercises, subGroupExercises, userId, subGroupId)
        return subGroupExercises.map { exercise ->
            updateExerciseDto(exercise.toDto(openSubGroupExercises.contains(exercise)))
        }
    }

    fun getAvailableExerciseIds(exerciseIds: List<Long>): List<Long> {
        if (exerciseIds.isEmpty()) return emptyList()
        val exercise = exerciseRepository.findById(exerciseIds[0])
        if (!exercise.isPresent) throw EntityNotFoundException("There is no one exercise with id = ${exerciseIds[0]}")
        val currentUserId = userAccountService.getCurrentUserId()
        return findExercisesByUserIdAndSubGroupId(currentUserId, exercise.get().subGroup!!.id!!)
            .filter(ExerciseDto::available)
            .map { e -> e.id!! }
    }

    fun getAvailableExercisesForSubGroup(
        doneSubGroupExercises: List<Exercise>,
        subGroupExercises: List<Exercise>,
        userId: Long,
        subGroupId: Long
    ): Set<Exercise> {
        if (doneSubGroupExercises.size == subGroupExercises.size)
            return doneSubGroupExercises.toSet()
        val mapDoneNameToExercise = doneSubGroupExercises.groupBy({ it.name }, { it })
        val availableExercises = mutableSetOf<Exercise>()
        val lastHistoryMap = studyHistoryRepository.findLastBySubGroupAndUserAccount(subGroupId, userId)
            .groupBy({ it.exercise }, { it })
        subGroupExercises
            .groupBy({ it.name }, { it })
            .forEach { (name, currentNameExercises) ->
                run {
                    availableExercises.add(currentNameExercises[0])
                    val currentDoneExercises = mapDoneNameToExercise[name]
                    if (currentDoneExercises.isNullOrEmpty()) {
                        availableExercises.add(currentNameExercises[0])
                        return@forEach
                    }
                    val lastDoneExercise = currentDoneExercises.last()
                    val lastHistory = lastHistoryMap[lastDoneExercise]
                    if (lastHistory.isNullOrEmpty()) {
                        availableExercises.addAll(currentDoneExercises)
                        return@forEach
                    }
                    if (!isDoneWell(lastHistory[0])) {
                        availableExercises.addAll(currentDoneExercises)
                        return@forEach
                    }
                    availableExercises.addAll(currentDoneExercises)
                    val closedExercises = currentNameExercises.minus(doneSubGroupExercises)
                    if (closedExercises.isNotEmpty())
                        availableExercises.add(closedExercises.first())
                }
            }
        return availableExercises
    }

    fun isDoneWell(studyHistory: StudyHistory): Boolean {
        val repetitionIndex = studyHistory.tasksCount.toFloat() / (studyHistory.replaysCount + studyHistory.tasksCount)
        val rightAnswersIndex = 1F - studyHistory.wrongAnswers.toFloat() / studyHistory.tasksCount
        return (repetitionIndex >= minRepetitionIndex.toFloat() && rightAnswersIndex >= minRightAnswersIndex.toFloat())
    }

    fun updateExerciseDto(exerciseDto: ExerciseDto): ExerciseDto {
        exerciseDto.noise.url = urlConversionService.makeUrlForNoise(exerciseDto.noise.url)
        return exerciseDto
    }

    fun updateActiveStatus(exerciseId: Long, active: Boolean) {
        var exercise = exerciseRepository.findById(exerciseId).get()
        exercise.active = active
        exerciseRepository.save(exercise)
    }

    fun findExercisesWithTasksBySubGroup(subGroupId: Long): List<ExerciseDto> =
        exerciseRepository
            .findExercisesBySubGroupId(subGroupId)
            .map { updateExerciseDto(it.toDto()) }

    fun findExercisesByWord(word: String): List<ExerciseWithWordsResponse> =
        exerciseRepository
            .findExercisesByWord(word)
            .map { it.toDtoWithWords() }

    @Transactional(rollbackFor = [Exception::class])
    fun createExercise(exerciseCreateDto: ExerciseCreateDto): ExerciseDto {
        val exercise = when (exerciseCreateDto) {
            is ExerciseWordsCreateDto -> {
                val seriesWordsRecord = exerciseCreateDto.toSeriesWordsRecord()
                val exercise = createExercise(seriesWordsRecord, exerciseCreateDto.locale)
                    ?: throw IllegalArgumentException("Exercise with this name (${exerciseCreateDto.exerciseName}) already exist")
                exercise
            }
            is ExercisePhrasesCreateDto -> {
                val seriesPhrasesRecord = exerciseCreateDto.toSeriesPhrasesRecord()
                val exercise = createExercise(seriesPhrasesRecord, exerciseCreateDto.locale)
                    ?: throw IllegalArgumentException("Exercise with this name (${exerciseCreateDto.exerciseName}) already exist")
                exercise
            }
            is ExerciseSentencesCreateDto -> {
                val seriesMatrixRecord = exerciseCreateDto.toSeriesMatrixRecord()
                val exercise = createExercise(seriesMatrixRecord, exerciseCreateDto.locale)
                    ?: throw IllegalArgumentException("Exercise with this name (${exerciseCreateDto.exerciseName}) already exist")
                exercise
            }
        }
        return exercise.toDto()
    }

    private fun createExercise(exerciseRecord: Any, locale: BrnLocale): Exercise? =
        recordProcessors.stream()
            .filter { it.isApplicable(exerciseRecord) }
            .findFirst()
            .orElseThrow { RuntimeException("There is no applicable processor for type '${exerciseRecord.javaClass}'") }
            .process(listOf(exerciseRecord) as List<Nothing>, locale)
            .firstOrNull() as Exercise?
}
