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
import com.epam.brn.model.projection.ExerciseAvailabilityView
import com.epam.brn.model.projection.ExerciseLastAttemptView
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

    @Transactional(readOnly = true)
    fun findExerciseById(exerciseID: Long): ExerciseDto {
        val exercise =
            exerciseRepository.findByIdWithSubGroup(exerciseID)
                ?: throw EntityNotFoundException("Could not find requested exerciseID=$exerciseID")
        return updateExerciseDto(exercise.toDto())
    }

    fun findExerciseByNameAndLevel(
        name: String,
        level: Int,
    ): Exercise = exerciseRepository
        .findExerciseByNameAndLevel(name, level)
        .orElseThrow { EntityNotFoundException("Exercise was not found by name=$name and level=$level") }

    @Transactional(readOnly = true)
    fun findExercisesByUserId(userId: Long): List<ExerciseDto> {
        log.info("Searching available exercises for user=$userId")
        val exercisesIdList = studyHistoryRepository.getDoneExercisesIdList(userId)
        val exercises = exerciseRepository.findAll()
        return exercises.map { exercise ->
            updateExerciseDto(exercise.toDto(exercisesIdList.contains(exercise.id)))
        }
    }

    @Transactional(readOnly = true)
    fun findExercisesBySubGroupForCurrentUser(subGroupId: Long): List<ExerciseDto> {
        val currentUserId = userAccountService.getCurrentUserId()
        return findExercisesByUserIdAndSubGroupId(currentUserId, subGroupId)
    }

    @Transactional(readOnly = true)
    fun findExercisesByUserIdAndSubGroupId(
        userId: Long,
        subGroupId: Long,
    ): List<ExerciseDto> {
        log.debug("Searching exercises for user=$userId with subGroupId=$subGroupId with Availability")
        val subGroupExercises = exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId).sortedBy { s -> s.level }
        val currentUserRoles = userAccountService.getCurrentUserRoles()
        if (currentUserRoles.contains(BrnRole.ADMIN) || currentUserRoles.contains(BrnRole.SPECIALIST))
            return subGroupExercises.map { exercise -> updateExerciseDto(exercise.toDto(true)) }
        val doneSubGroupExercises = studyHistoryRepository.getDoneExercises(subGroupId, userId)
        val openSubGroupExercises =
            getAvailableExercisesForSubGroup(doneSubGroupExercises, subGroupExercises, userId, subGroupId)
        return subGroupExercises
            .mapIndexed { index, exercise ->
                val updatedExerciseDto =
                    updateExerciseDto(exercise.toDto(openSubGroupExercises.contains(exercise)))
                updatedExerciseDto.level = index + 1
                updatedExerciseDto
            }
    }

    @Transactional(readOnly = true)
    fun getAvailableExerciseIds(exerciseIds: List<Long>): List<Long> {
        if (exerciseIds.isEmpty()) return emptyList()
        val exerciseId = exerciseIds[0]
        val subGroupId =
            exerciseRepository.findSubGroupIdByExerciseId(exerciseId)
                ?: throw EntityNotFoundException("There is no one exercise with id = $exerciseId")
        val currentUser = userAccountService.getCurrentUser()
        val currentUserId = currentUser.id!!
        val currentUserRoles = currentUser.roleSet.map { it.name }.toSet()
        if (currentUserRoles.contains(BrnRole.ADMIN) || currentUserRoles.contains(BrnRole.SPECIALIST)) {
            return exerciseRepository.findExerciseIdsBySubGroupId(subGroupId)
        }
        val subGroupExercises = exerciseRepository.findExerciseAvailabilityBySubGroupId(subGroupId)
        val doneExerciseIds = studyHistoryRepository.getDoneExerciseIds(subGroupId, currentUserId).toSet()
        val lastAttemptsByExerciseId =
            studyHistoryRepository.findLastAttemptBySubGroupAndUserAccount(subGroupId, currentUserId)
                .associateBy { it.exerciseId }
        return calculateAvailableExerciseIds(subGroupExercises, doneExerciseIds, lastAttemptsByExerciseId)
    }

    fun getAvailableExercisesForSubGroup(
        doneSubGroupExercises: List<Exercise>,
        subGroupExercises: List<Exercise>,
        userId: Long,
        subGroupId: Long,
    ): Set<Exercise> {
        if (doneSubGroupExercises.size == subGroupExercises.size)
            return doneSubGroupExercises.toSet()
        val mapDoneNameToExercise = doneSubGroupExercises.groupBy({ it.name }, { it })
        val availableExercises = mutableSetOf<Exercise>()
        val lastHistoryMap =
            studyHistoryRepository
                .findLastBySubGroupAndUserAccount(subGroupId, userId)
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

    private fun isDoneWell(lastAttempt: ExerciseLastAttemptView): Boolean {
        val repetitionIndex = lastAttempt.tasksCount.toFloat() / (lastAttempt.replaysCount + lastAttempt.tasksCount)
        val rightAnswersIndex = 1F - lastAttempt.wrongAnswers.toFloat() / lastAttempt.tasksCount
        return (repetitionIndex >= minRepetitionIndex.toFloat() && rightAnswersIndex >= minRightAnswersIndex.toFloat())
    }

    fun updateExerciseDto(exerciseDto: ExerciseDto): ExerciseDto {
        exerciseDto.noise.url = urlConversionService.makeUrlForNoise(exerciseDto.noise.url)
        return exerciseDto
    }

    fun updateActiveStatus(
        exerciseId: Long,
        active: Boolean,
    ) {
        var exercise = exerciseRepository.findById(exerciseId).get()
        exercise.active = active
        exerciseRepository.save(exercise)
    }

    @Transactional(readOnly = true)
    fun findExercisesWithTasksBySubGroup(subGroupId: Long): List<ExerciseDto> = exerciseRepository
        .findExercisesWithSubGroupBySubGroupId(subGroupId)
        .map { updateExerciseDto(it.toDto()) }

    @Transactional(readOnly = true)
    fun findExercisesByWord(word: String): List<ExerciseWithWordsResponse> = exerciseRepository
        .findExercisesByWord(word)
        .map { it.toDtoWithWords() }

    @Transactional(rollbackFor = [Exception::class])
    fun createExercise(exerciseCreateDto: ExerciseCreateDto): ExerciseDto {
        val exercise =
            when (exerciseCreateDto) {
                is ExerciseWordsCreateDto -> {
                    val seriesWordsRecord = exerciseCreateDto.toSeriesWordsRecord()
                    val exercise =
                        createExercise(seriesWordsRecord, exerciseCreateDto.locale)
                            ?: throw IllegalArgumentException("Exercise with this name (${exerciseCreateDto.exerciseName}) already exist")
                    exercise
                }
                is ExercisePhrasesCreateDto -> {
                    val seriesPhrasesRecord = exerciseCreateDto.toSeriesPhrasesRecord()
                    val exercise =
                        createExercise(seriesPhrasesRecord, exerciseCreateDto.locale)
                            ?: throw IllegalArgumentException("Exercise with this name (${exerciseCreateDto.exerciseName}) already exist")
                    exercise
                }
                is ExerciseSentencesCreateDto -> {
                    val seriesMatrixRecord = exerciseCreateDto.toSeriesMatrixRecord()
                    val exercise =
                        createExercise(seriesMatrixRecord, exerciseCreateDto.locale)
                            ?: throw IllegalArgumentException("Exercise with this name (${exerciseCreateDto.exerciseName}) already exist")
                    exercise
                }
            }
        return exercise.toDto()
    }

    private fun createExercise(
        exerciseRecord: Any,
        locale: BrnLocale,
    ): Exercise? = recordProcessors
        .stream()
        .filter { it.isApplicable(exerciseRecord) }
        .findFirst()
        .orElseThrow { RuntimeException("There is no applicable processor for type '${exerciseRecord.javaClass}'") }
        .process(listOf(exerciseRecord) as List<Nothing>, locale)
        .firstOrNull() as Exercise?

    private fun calculateAvailableExerciseIds(
        subGroupExercises: List<ExerciseAvailabilityView>,
        doneExerciseIds: Set<Long>,
        lastAttemptsByExerciseId: Map<Long, ExerciseLastAttemptView>,
    ): List<Long> {
        if (subGroupExercises.isEmpty()) return emptyList()
        if (doneExerciseIds.size == subGroupExercises.size) return subGroupExercises.map(ExerciseAvailabilityView::id)

        val availableExerciseIds = linkedSetOf<Long>()
        subGroupExercises
            .groupBy(ExerciseAvailabilityView::name)
            .forEach { (_, currentNameExercises) ->
                val firstExercise = currentNameExercises.first()
                availableExerciseIds.add(firstExercise.id)

                val currentDoneExercises = currentNameExercises.filter { doneExerciseIds.contains(it.id) }
                if (currentDoneExercises.isEmpty()) return@forEach

                availableExerciseIds.addAll(currentDoneExercises.map(ExerciseAvailabilityView::id))
                val lastDoneExercise = currentDoneExercises.last()
                val lastAttempt = lastAttemptsByExerciseId[lastDoneExercise.id] ?: return@forEach
                if (!isDoneWell(lastAttempt)) return@forEach

                val nextClosedExercise = currentNameExercises.firstOrNull { !doneExerciseIds.contains(it.id) } ?: return@forEach
                availableExerciseIds.add(nextClosedExercise.id)
            }
        return availableExerciseIds.toList()
    }
}
