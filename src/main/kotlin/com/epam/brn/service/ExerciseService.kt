package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import org.apache.commons.collections4.CollectionUtils.emptyIfNull
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
        return emptyIfNull(history).map { x -> updateNoiseUrl(x.toDto(exercisesIdList.contains(x.id))) }
    }

    fun findAllExercisesBySeriesForCurrentUser(seriesId: Long, withAvailability: Boolean): List<ExerciseDto> {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return findExercisesByUserIdAndSeries(currentUser.id!!, seriesId, withAvailability)
    }

    fun findExercisesByNameForCurrentUser(exerciseName: String): List<ExerciseDto> {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return findExercisesByNameAndUserId(currentUser.id!!, exerciseName)
    }

    fun findExercisesByUserIdAndSeries(userId: Long, seriesId: Long, withAvailability: Boolean): List<ExerciseDto> {
        log.info("Searching exercises for user=$userId with series=$seriesId, withAvailability=$withAvailability")
        val allExercises = exerciseRepository.findExercisesBySeriesId(seriesId)
        log.info("current user is admin: ${userId == 1L}))")
        if (!withAvailability || userId == 1L)
            return emptyIfNull(allExercises).map { exercise -> updateNoiseUrl(exercise.toDto(true)) }
        val doneExercises = studyHistoryRepository.getDoneExercises(seriesId, userId)
        val openExercises = getAvailableExercises(doneExercises, allExercises, userId)
        return emptyIfNull(allExercises).map { exercise ->
            updateNoiseUrl(exercise.toDto(openExercises.contains(exercise)))
        }
    }

    fun findExercisesByNameAndUserId(userId: Long, exerciseName: String): List<ExerciseDto> {
        log.info("Searching available exercises with name=$exerciseName for user=$userId")
        val isSupport = userId == 1L
        log.info("current user is admin: $isSupport")
        val doneExercises = studyHistoryRepository.getDoneExercisesByName(exerciseName, userId)
        val allExercises = exerciseRepository.findExercisesByName(exerciseName)
        val openExercises = getAvailableExercises(doneExercises, allExercises, userId)
        return emptyIfNull(allExercises).map { exercise ->
            updateNoiseUrl(exercise.toDto(openExercises.contains(exercise) || isSupport))
        }
    }

    fun getAvailableExercises(
        doneExercises: List<Exercise>,
        allExercises: List<Exercise>,
        userId: Long
    ): Set<Exercise> {
        if (doneExercises.size == allExercises.size)
            return doneExercises.toSet()
        val mapDone = doneExercises.groupBy({ it.name }, { it })
        val available = mutableSetOf<Exercise>()
        val lastHistoryMap = studyHistoryRepository.findLastByUserAccountId(userId)
            .groupBy({ it.exercise }, { it })
        allExercises
            .groupBy({ it.name }, { it })
            .forEach { (name, currentNameExercises) ->
                run {
                    available.add(currentNameExercises[0])
                    val currentDone = mapDone[name]
                    if (currentDone.isNullOrEmpty()) {
                        available.add(currentNameExercises[0])
                        return@forEach
                    }
                    val lastDone = currentDone?.last()
                    val lastHistory = lastHistoryMap[lastDone]
                    if (lastHistory.isNullOrEmpty()) {
                        available.addAll(currentDone)
                        return@forEach
                    }
                    val repetitionIndex = lastHistory[0].tasksCount!!.toFloat() / lastHistory[0].listeningsCount!!
                    val rightAnswersIndex = lastHistory[0].rightAnswersCount!!.toFloat() / lastHistory[0].tasksCount!!
                    if (repetitionIndex < minRepetitionIndex.toFloat() || rightAnswersIndex < minRightAnswersIndex.toFloat()) {
                        available.addAll(currentDone)
                        return@forEach
                    }
                    available.addAll(currentDone)
                    val closed = currentNameExercises.minus(doneExercises)
                    if (closed.isNotEmpty())
                        available.add(closed.first())
                }
            }
        return available
    }

    fun updateNoiseUrl(exerciseDto: ExerciseDto): ExerciseDto {
        exerciseDto.noise.url = urlConversionService.makeFullUrl(exerciseDto.noise.url)
        return exerciseDto
    }
}
