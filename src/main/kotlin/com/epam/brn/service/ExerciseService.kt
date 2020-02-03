package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import org.apache.commons.collections4.CollectionUtils.emptyIfNull
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ExerciseService(
    @Autowired val exerciseRepository: ExerciseRepository,
    @Autowired val studyHistoryRepository: StudyHistoryRepository
) {
    private val log = logger()

    fun findExerciseById(exerciseID: Long): ExerciseDto {
        val exercise = exerciseRepository.findById(exerciseID)
        return exercise.map { e -> e.toDto() }
            .orElseThrow { NoDataFoundException("Could not find requested exerciseID=$exerciseID") }
    }

    fun findExerciseEntityByName(name: String): Exercise {
        return exerciseRepository.findExerciseByName(name)
            .orElseThrow { EntityNotFoundException("Exercise entity was not found by name $name") }
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

    fun save(exercise: Exercise): Exercise {
        return exerciseRepository.save(exercise)
    }

    fun createExercise(name: String): Exercise {
        val exercise = Exercise()
        exercise.name = name
        exerciseRepository.save(exercise)

        return exercise
    }
}
