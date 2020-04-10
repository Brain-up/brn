package com.epam.brn.service

import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.TaskRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val exerciseRepository: ExerciseRepository,
    private val resourceRepository: ResourceRepository
) {
    private val log = logger()

    fun getTasksByExerciseId(exerciseId: Long): List<Any> {
        val exercise: Exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow { EntityNotFoundException("No exercise found for id=$exerciseId") }
        val tasks = taskRepository.findTasksByExerciseIdWithJoinedAnswers(exerciseId)
        return when (ExerciseType.valueOf(exercise.exerciseType)) {
            ExerciseType.SINGLE_WORDS -> tasks.map { task -> task.to1SeriesTaskDto() }
            ExerciseType.WORDS_SEQUENCES -> tasks.map { task -> task.to2SeriesTaskDto(task.exercise?.template) }
            ExerciseType.SENTENCE -> tasks.map { task -> task.to3SeriesTaskDto(task.exercise?.template) }
            ExerciseType.SINGLE_SIMPLE_WORDS -> tasks.map { task -> task.to4SeriesTaskDto() }
        }
    }

    fun getTaskById(taskId: Long): Any {
        log.debug("Searching task with id=$taskId")
        val task =
            taskRepository.findById(taskId).orElseThrow { EntityNotFoundException("No task found for id=$taskId") }
        return when (ExerciseType.valueOf(task.exercise!!.exerciseType)) {
            ExerciseType.SINGLE_WORDS -> task.to1SeriesTaskDto()
            ExerciseType.WORDS_SEQUENCES -> task.to2SeriesTaskDto(task.exercise?.template)
            ExerciseType.SENTENCE -> task.to3SeriesTaskDto(task.exercise?.template)
            ExerciseType.SINGLE_SIMPLE_WORDS -> task.to4SeriesTaskDto()
        }
    }

    @Transactional
    fun save(task: Task): Task {
        val resources = mutableSetOf<Resource>()
        resources.addAll(task.answerOptions)
        task.correctAnswer?.let { resources.add(it) }
        resourceRepository.saveAll(resources)
        return taskRepository.save(task)
    }
}
