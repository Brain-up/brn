package com.epam.brn.service

import com.epam.brn.dto.TaskDto
import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.model.Task
import com.epam.brn.repo.TaskRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
@CacheConfig(cacheNames = arrayOf("tasks"))
class TaskService(private val taskRepository: TaskRepository) {

    private val log = logger()

    @Cacheable
    fun getAllTasksByExerciseId(exerciseId: Long): List<TaskDto> {
        val tasks = taskRepository.findAllTasksByExerciseIdWithJoinedAnswers(exerciseId)
        return tasks.map { task -> task.toDto() }
    }

    fun getAllTasks(): List<TaskDto> {
        val tasks = taskRepository.findAllTasksWithJoinedAnswers()
        return tasks.map { task -> task.toDto() }
    }

    fun save(task: Task): Task {
        return taskRepository.save(task)
    }

    @CacheEvict("tasks")
    fun getTaskById(taskId: Long): TaskDto {
        log.debug("Searching task with id=$taskId")
        return taskRepository.findById(taskId)
            .map { it.toDto() }
            .orElseThrow { NoDataFoundException("no task is found for id=$taskId") }
    }
}