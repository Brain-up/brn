package com.epam.brn.service

import com.epam.brn.dto.TaskDto
import com.epam.brn.repo.TaskRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class TaskService(private val taskRepository: TaskRepository) {

    private val log = logger()

    fun findAllTasksWithExerciseIdWithAnswers(exerciseId: String): List<TaskDto> {
        val tasks = taskRepository.findAllTasksWithExerciseIdWithAnswers(exerciseId.toLong())
        return tasks.map { task -> task.toDto() }
    }

    fun findAllTasksWithAnswers(): List<TaskDto> {
        val tasks = taskRepository.findAllTasksWithAnswers()
        return tasks.map { task -> task.toDto() }
    }
}