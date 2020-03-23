package com.epam.brn.service

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.TaskRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val exerciseRepository: ExerciseRepository
) {
    private val log = logger()

    fun getTasksByExerciseId(exerciseId: Long): List<Any> {
        val exercise: Exercise = exerciseRepository.findById(exerciseId).orElseThrow { EntityNotFoundException("No exercise found for id=$exerciseId") }
        val tasks = taskRepository.findTasksByExerciseIdWithJoinedAnswers(exerciseId)
        return when (ExerciseTypeEnum.valueOf(exercise.exerciseType)) {
            ExerciseTypeEnum.SINGLE_WORDS -> tasks.map { task -> task.toSingleWordsDto() }
            ExerciseTypeEnum.WORDS_SEQUENCES -> tasks.map { task -> task.toSequenceWordsDto(task.exercise?.template) }
            ExerciseTypeEnum.SENTENCE -> tasks.map { task -> task.toSentenceDto(task.exercise?.template) }
        }
    }

    fun getTaskById(taskId: Long): Any {
        log.debug("Searching task with id=$taskId")
        val task = taskRepository.findById(taskId).orElseThrow { EntityNotFoundException("No task found for id=$taskId") }
        return when (ExerciseTypeEnum.valueOf(task.exercise!!.exerciseType)) {
            ExerciseTypeEnum.SINGLE_WORDS -> task.toSingleWordsDto()
            ExerciseTypeEnum.WORDS_SEQUENCES -> task.toSequenceWordsDto(task.exercise?.template)
            ExerciseTypeEnum.SENTENCE -> task.toSentenceDto(task.exercise?.template)
        }
    }
}
