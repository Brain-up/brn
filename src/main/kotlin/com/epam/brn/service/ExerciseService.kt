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
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.upload.csv.RecordProcessor
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
    private val urlConversionService: UrlConversionService,
    private val taskService: TaskService,
    private val recordProcessors: List<RecordProcessor<out Any, out Any>>,
    private val exerciseSuccessCalculator: ExerciseSuccessCalculator,
) {
    private val log = logger()

    @Transactional(readOnly = true)
    fun findExerciseById(exerciseID: Long): ExerciseDto {
        val exercise =
            exerciseRepository.findByIdWithSubGroup(exerciseID)
                ?: throw EntityNotFoundException("Could not find requested exerciseID=$exerciseID")
        updateTasksUrl(exercise)
        return updateNoiseExerciseDto(exercise.toDto())
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
            updateTasksUrl(exercise)
            updateNoiseExerciseDto(exercise.toDto(exercisesIdList.contains(exercise.id)))
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
        val subGroupExercises =
            exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId).sortedBy { s -> s.level }
        val currentUserRoles = userAccountService.getCurrentUserRoles()
        if (currentUserRoles.contains(BrnRole.ADMIN) || currentUserRoles.contains(BrnRole.SPECIALIST))
            return subGroupExercises.map { exercise ->
                updateTasksUrl(exercise)
                updateNoiseExerciseDto(exercise.toDto(true))
            }
        val doneSubGroupExercises = studyHistoryRepository.getDoneExercises(subGroupId, userId)
        val openSubGroupExercises =
            getAvailableExercisesForSubGroup(doneSubGroupExercises, subGroupExercises, userId, subGroupId)
        return subGroupExercises
            .mapIndexed { index, exercise ->
                updateTasksUrl(exercise)
                val updatedExerciseDto =
                    updateNoiseExerciseDto(exercise.toDto(openSubGroupExercises.contains(exercise)))
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
        val everPassedExerciseIds = findEverPassedExerciseIds(subGroupId, currentUserId)
        return calculateAvailableExerciseIds(subGroupExercises, doneExerciseIds, everPassedExerciseIds)
    }

    fun getAvailableExercisesForSubGroup(
        doneSubGroupExercises: List<Exercise>,
        subGroupExercises: List<Exercise>,
        userId: Long,
        subGroupId: Long,
    ): Set<Exercise> {
        if (doneSubGroupExercises.size == subGroupExercises.size)
            return doneSubGroupExercises.toSet()
        val everPassedExerciseIds = findEverPassedExerciseIds(subGroupId, userId)
        val availableIds =
            computeAvailableExerciseIds(
                subGroupExercises.map { AvailabilityExercise(it.id!!, it.name) },
                doneSubGroupExercises.mapNotNull { it.id }.toSet(),
                everPassedExerciseIds,
            ).toSet()
        return subGroupExercises.filter { it.id in availableIds }.toSet()
    }

    /**
     * Exercise ids in the subgroup that the user has done well in at least one recorded attempt.
     * Derived from the full attempt history (not only the last attempt) so that unlocking is sticky.
     */
    private fun findEverPassedExerciseIds(
        subGroupId: Long,
        userId: Long,
    ): Set<Long> = studyHistoryRepository
        .findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId)
        .filter { exerciseSuccessCalculator.isSuccessful(it) }
        .map { it.exerciseId }
        .toSet()

    fun isDoneWell(studyHistory: StudyHistory): Boolean = exerciseSuccessCalculator.isSuccessful(studyHistory)

    fun updateNoiseExerciseDto(exerciseDto: ExerciseDto): ExerciseDto {
        exerciseDto.noise.url = urlConversionService.makeUrlForNoise(exerciseDto.noise.url)
        return exerciseDto
    }

    fun updateTasksUrl(exercise: Exercise) {
        exercise.tasks.forEach { task -> taskService.processAnswerOptions(task) }
    }

    fun updateActiveStatus(
        exerciseId: Long,
        active: Boolean,
    ) {
        val exercise = exerciseRepository.findById(exerciseId).get()
        exercise.active = active
        exerciseRepository.save(exercise)
    }

    @Transactional(readOnly = true)
    fun findExercisesWithTasksBySubGroup(subGroupId: Long): List<ExerciseDto> = exerciseRepository
        .findExercisesWithSubGroupBySubGroupId(subGroupId)
        .map { updateNoiseExerciseDto(it.toDto()) }

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
        everPassedExerciseIds: Set<Long>,
    ): List<Long> = computeAvailableExerciseIds(
        subGroupExercises.map { AvailabilityExercise(it.id, it.name) },
        doneExerciseIds,
        everPassedExerciseIds,
    )

    /**
     * Single source of truth for progressive exercise unlocking within a subgroup, shared by both
     * the entity-based ([getAvailableExercisesForSubGroup]) and projection-based
     * ([calculateAvailableExerciseIds]) access paths. Works on minimal inputs — exercises in order
     * (`id`, `name`), the set of done ids, and the set of ids done well at least once — and returns
     * the available ids preserving the input order.
     *
     * Rules: the first exercise of every name is always available; every done exercise is available;
     * the next not-yet-done exercise of a name is unlocked when the last done exercise of that name was
     * done well in any attempt. Unlocking is sticky — a later unsuccessful attempt never re-locks it.
     */
    private fun computeAvailableExerciseIds(
        subGroupExercises: List<AvailabilityExercise>,
        doneExerciseIds: Set<Long>,
        everPassedExerciseIds: Set<Long>,
    ): List<Long> {
        if (subGroupExercises.isEmpty()) return emptyList()
        if (doneExerciseIds.size == subGroupExercises.size) return subGroupExercises.map(AvailabilityExercise::id)

        val availableExerciseIds = linkedSetOf<Long>()
        subGroupExercises
            .groupBy(AvailabilityExercise::name)
            .forEach { (_, currentNameExercises) ->
                val firstExercise = currentNameExercises.first()
                availableExerciseIds.add(firstExercise.id)

                val currentDoneExercises = currentNameExercises.filter { doneExerciseIds.contains(it.id) }
                if (currentDoneExercises.isEmpty()) return@forEach

                availableExerciseIds.addAll(currentDoneExercises.map(AvailabilityExercise::id))
                val lastDoneExercise = currentDoneExercises.last()
                if (lastDoneExercise.id !in everPassedExerciseIds) return@forEach

                val nextClosedExercise =
                    currentNameExercises.firstOrNull { !doneExerciseIds.contains(it.id) } ?: return@forEach
                availableExerciseIds.add(nextClosedExercise.id)
            }
        return subGroupExercises
            .map(AvailabilityExercise::id)
            .filter(availableExerciseIds::contains)
    }

    private data class AvailabilityExercise(
        val id: Long,
        val name: String,
    )
}
