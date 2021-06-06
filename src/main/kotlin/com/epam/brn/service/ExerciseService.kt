package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.ExerciseWithTasksDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ExerciseService(
    @Autowired val exerciseRepository: ExerciseRepository,
    @Autowired val studyHistoryRepository: StudyHistoryRepository,
    @Autowired val userAccountService: UserAccountService,
    @Autowired val urlConversionService: UrlConversionService
) {

    @Value(value = "\${minRepetitionIndex}")
    private lateinit var minRepetitionIndex: Number

    @Value(value = "\${minRightAnswersIndex}")
    private lateinit var minRightAnswersIndex: Number

    private val log = logger()

    fun findExerciseById(exerciseID: Long): ExerciseDto {
        val exercise = exerciseRepository.findById(exerciseID)
        return exercise.map { e -> updateNoiseUrl(e.toDto()) }
            .orElseThrow { EntityNotFoundException("Could not find requested exerciseID=$exerciseID") }
    }

    fun findExerciseByNameAndLevel(name: String, level: Int): Exercise {
        return exerciseRepository.findExerciseByNameAndLevel(name, level)
            .orElseThrow { EntityNotFoundException("Exercise was not found by name=$name and level=$level") }
    }

    fun findExercisesByUserId(userId: Long): List<ExerciseDto> {
        log.info("Searching available exercises for user=$userId")
        val exercisesIdList = studyHistoryRepository.getDoneExercisesIdList(userId)
        val history = exerciseRepository.findAll()
        return history.map { x -> updateNoiseUrl(x.toDto(exercisesIdList.contains(x.id))) }
    }

    fun findExercisesBySubGroupForCurrentUser(subGroupId: Long): List<ExerciseDto> {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return findExercisesByUserIdAndSubGroupId(currentUser.id!!, subGroupId)
    }

    fun findExercisesByUserIdAndSubGroupId(userId: Long, subGroupId: Long): List<ExerciseDto> {
        log.info("Searching exercises for user=$userId with subGroupId=$subGroupId with Availability")
        val subGroupExercises = exerciseRepository.findExercisesBySubGroupId(subGroupId)
        log.info("current user is admin: ${userId == 1L}))")
        if (userId == 1L)
            return subGroupExercises.map { exercise -> updateNoiseUrl(exercise.toDto(true)) }
        val doneSubGroupExercises = studyHistoryRepository.getDoneExercises(subGroupId, userId)
        val openSubGroupExercises = getAvailableExercises(doneSubGroupExercises, subGroupExercises, userId)
        return subGroupExercises.map { exercise ->
            updateNoiseUrl(exercise.toDto(openSubGroupExercises.contains(exercise)))
        }
    }

    fun getAvailableExerciseIds(exerciseIds: List<Long>): List<Long> {
        if (exerciseIds.isEmpty()) return emptyList()
        val exercise = exerciseRepository.findById(exerciseIds[0])
        if (!exercise.isPresent) throw EntityNotFoundException("There is no one exercise with id = ${exerciseIds[0]}")
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return findExercisesByUserIdAndSubGroupId(currentUser.id!!, exercise.get().subGroup!!.id!!)
            .filter(ExerciseDto::available)
            .map { e -> e.id!! }
    }

    fun getAvailableExercises(
        doneSubGroupExercises: List<Exercise>,
        subGroupExercises: List<Exercise>,
        userId: Long
    ): Set<Exercise> {
        if (doneSubGroupExercises.size == subGroupExercises.size)
            return doneSubGroupExercises.toSet()
        val mapDone = doneSubGroupExercises.groupBy({ it.name }, { it })
        val available = mutableSetOf<Exercise>()
        val lastHistoryMap = studyHistoryRepository.findLastByUserAccountId(userId)
            .groupBy({ it.exercise }, { it })
        subGroupExercises
            .groupBy({ it.name }, { it })
            .forEach { (name, currentNameExercises) ->
                run {
                    available.add(currentNameExercises[0])
                    val currentDone = mapDone[name]
                    if (currentDone.isNullOrEmpty()) {
                        available.add(currentNameExercises[0])
                        return@forEach
                    }
                    val lastDone = currentDone.last()
                    val lastHistory = lastHistoryMap[lastDone]
                    if (lastHistory.isNullOrEmpty()) {
                        available.addAll(currentDone)
                        return@forEach
                    }
                    val repetitionIndex =
                        lastHistory[0].tasksCount.toFloat() / (lastHistory[0].replaysCount + lastHistory[0].tasksCount)
                    val rightAnswersIndex = 1F - lastHistory[0].wrongAnswers.toFloat() / lastHistory[0].tasksCount
                    if (repetitionIndex < minRepetitionIndex.toFloat() || rightAnswersIndex < minRightAnswersIndex.toFloat()) {
                        available.addAll(currentDone)
                        return@forEach
                    }
                    available.addAll(currentDone)
                    val closed = currentNameExercises.minus(doneSubGroupExercises)
                    if (closed.isNotEmpty())
                        available.add(closed.first())
                }
            }
        return available
    }

    fun updateNoiseUrl(exerciseDto: ExerciseDto): ExerciseDto {
        exerciseDto.noise.url = urlConversionService.makeUrlForNoise(exerciseDto.noise.url)
        return exerciseDto
    }

    fun updateActiveStatus(exerciseId: Long, active: Boolean) {
        var exercise = exerciseRepository.findById(exerciseId).get()
        exercise.active = active
        exerciseRepository.save(exercise)
    }

    fun findExercisesWithTasksBySubGroup(subGroupId: Long): List<ExerciseWithTasksDto> {
        val subGroupExercises = exerciseRepository.findExercisesBySubGroupId(subGroupId)
        return subGroupExercises.map { it.toDtoWithTasks() }
    }
}
