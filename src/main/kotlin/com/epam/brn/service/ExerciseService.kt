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

    fun findExercisesBySeriesForCurrentUser(seriesId: Long): List<ExerciseDto> {
        val currentUser = userAccountService.getUserFromTheCurrentSession()
        return findExercisesByUserIdAndSeries(currentUser.id!!, seriesId)
    }

    fun findExercisesByUserIdAndSeries(userId: Long, seriesId: Long): List<ExerciseDto> {
        log.info("Searching available exercises for user=$userId with series=$seriesId")
        val isSupport = userId in (1..3)
        log.info("current user is admin: $isSupport")
        val doneExercises = studyHistoryRepository.getDoneExercisesIdList(seriesId, userId)
        val allExercises = exerciseRepository.findExercisesBySeriesId(seriesId)
        val openExercises = getAvailableExercises(doneExercises, allExercises, userId)
        return emptyIfNull(allExercises).map { exercise ->
            updateNoiseUrl(exercise.toDto(doneExercises.contains(exercise.id) || isSupport))
        }
    }

    fun getAvailableExercises(doneExercisesIds: List<Long>, allExercises: List<Exercise>, userId: Long): List<Long> {
        if (doneExercisesIds.size == allExercises.size)
            return doneExercisesIds
        val lastDoneId = doneExercisesIds.last()
        val lastStudyHistory = studyHistoryRepository.findByUserAccountIdAndExerciseId(userId, lastDoneId).get()
        if (lastStudyHistory != null) {
            val repetitionIndex = lastStudyHistory.tasksCount!!.toFloat() / lastStudyHistory.listeningsCount!!
            val rightAnswersIndex = lastStudyHistory.rightAnswersCount!!.toFloat() / lastStudyHistory.tasksCount!!
            if (repetitionIndex < minRepetitionIndex.toFloat() || rightAnswersIndex < minRightAnswersIndex.toFloat())
                return doneExercisesIds
        }

        val unavailableExercises = allExercises.map { e -> e.id }.minus(doneExercisesIds)
        val nextAvailable = unavailableExercises[0]!!
        val mutableList = doneExercisesIds.toMutableList()
        mutableList.add(nextAvailable)
        return mutableList
    }

    fun updateNoiseUrl(exerciseDto: ExerciseDto): ExerciseDto {
        exerciseDto.noise.url = urlConversionService.makeFullUrl(exerciseDto.noise.url)
        return exerciseDto
    }
}
