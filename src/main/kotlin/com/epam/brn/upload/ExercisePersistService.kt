package com.epam.brn.upload

import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.TaskRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ExercisePersistService(
    private val exerciseRepository: ExerciseRepository,
    private val taskRepository: TaskRepository
) {
    @Value("\${createOrUpdate}")
    var createOrUpdate: Boolean = false

    fun createOrUpdateExercise(newExercise: Exercise): Exercise {
        val existExercise = exerciseRepository.findByNameAndLevel(newExercise.name, newExercise.level)
        return if (existExercise == null)
            exerciseRepository.save(newExercise) // create
        else {
            newExercise.id = existExercise.id
            exerciseRepository.save(newExercise) // update existing entity
        }
    }

//    fun createOrUpdateTask(newTask: Task): Task {
//        val existTask = taskRepository.f
//        return if (existExercise == null)
//            exerciseRepository.save(newExercise) // create
//        else {
//            newExercise.id = existExercise.id
//            exerciseRepository.save(newExercise) // update existing entity
//        }
//    }
}
