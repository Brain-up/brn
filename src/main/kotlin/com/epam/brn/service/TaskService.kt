package com.epam.brn.service

import com.epam.brn.dto.TaskDto
import com.epam.brn.model.Task
import com.epam.brn.repo.TaskRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class TaskService(private val taskRepository: TaskRepository) {

    private val log = logger()

    fun findAllTasksByExerciseIdIncludeAnswers(exerciseId: Long): List<TaskDto> {
        val tasks = taskRepository.findAllTasksByExerciseIdWithJoinedAnswers(exerciseId)
        return tasks.map { task -> task.toDto() }
    }

    fun findAllTasksIncludeAnswers(): List<TaskDto> {
        val tasks = taskRepository.findAllTasksWithJoinedAnswers()
        return tasks.map { task -> task.toDto() }
    }

    fun save(task: Task): Task {
        return taskRepository.save(task)
    }
}