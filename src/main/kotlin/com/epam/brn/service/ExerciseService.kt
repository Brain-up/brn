package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import org.apache.commons.collections4.CollectionUtils.emptyIfNull
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExerciseService(
    @Autowired val exerciseRepository: ExerciseRepository,
    @Autowired val studyHistoryRepository: StudyHistoryRepository,
    private val taskService: TaskService
) {
    private val log = logger()

    fun findExerciseById(exerciseID: Long): ExerciseDto {
        val exercise = exerciseRepository.findById(exerciseID)
        return exercise.map { e -> e.toDto() }
            .orElseThrow { EntityNotFoundException("Could not find requested exerciseID=$exerciseID") }
    }

    fun findExerciseByNameAndLevel(name: String, level: Int): Exercise {
        return exerciseRepository.findExerciseByNameAndLevel(name, level)
            .orElseThrow { EntityNotFoundException("Exercise was not found by name=$name and level=$level") }
    }

    fun findExercisesByUserId(userId: Long): List<ExerciseDto> {
        log.debug("Searching available exercises for user=$userId")
        val exercisesIdList = studyHistoryRepository.getDoneExercisesIdList(userId)
        val history = exerciseRepository.findAll()
        return emptyIfNull(history).map { x -> x.toDto(exercisesIdList.contains(x.id)) }
    }

    fun findExercisesByUserIdAndSeries(userId: Long, seriesId: Long): List<ExerciseDto> {
        log.debug("Searching available exercises for user=$userId with series=$seriesId")
        val exercisesIdList = studyHistoryRepository.getDoneExercisesIdList(seriesId, userId)
        val exercises = exerciseRepository.findExercisesBySeriesId(seriesId)
        return emptyIfNull(exercises).map { x -> x.toDto(exercisesIdList.contains(x.id)) }
    }

    @Transactional
    fun save(exercise: Exercise): Exercise {
        val result = exerciseRepository.save(exercise)
        exercise.tasks.forEach {
            it.exercise = result
            taskService.save(it)
        }
        return result
    }

    @Transactional
    fun save(exercises: List<Exercise>): List<Exercise> = exercises.map(this::save)
}
