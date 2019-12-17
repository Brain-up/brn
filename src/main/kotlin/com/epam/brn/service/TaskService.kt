package com.epam.brn.service

import com.epam.brn.dto.TaskDtoForSingleWords
import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.model.Task
import com.epam.brn.repo.TaskRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class TaskService(private val taskRepository: TaskRepository) {

    private val log = logger()

    fun getAllTasksByExerciseId(exerciseId: Long): List<TaskDtoForSingleWords> {
        val tasks = taskRepository.findAllTasksByExerciseIdWithJoinedAnswers(exerciseId)
        return tasks.map { task -> task.toSingleWordsDto() }
    }

    fun getAllTasks(): List<TaskDtoForSingleWords> {
        val tasks = taskRepository.findAllTasksWithJoinedAnswers()
        return tasks.map { task -> task.toSingleWordsDto() }
    }

    fun save(task: Task): Task {
        return taskRepository.save(task)
    }

    fun getTaskById(taskId: Long): TaskDtoForSingleWords {
        log.debug("Searching task with id=$taskId")
        return taskRepository.findById(taskId)
            .map { it.toSingleWordsDto() }
            .orElseThrow { NoDataFoundException("no task is found for id=$taskId") }
    }
}