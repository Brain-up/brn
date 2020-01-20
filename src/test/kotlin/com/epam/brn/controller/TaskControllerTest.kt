package com.epam.brn.controller

import com.epam.brn.dto.TaskDtoForSingleWords
import com.epam.brn.service.TaskService
import com.nhaarman.mockito_kotlin.verify
import org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE
import org.apache.commons.lang3.math.NumberUtils.INTEGER_TWO
import org.apache.commons.lang3.math.NumberUtils.LONG_ONE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class TaskControllerTest {

    @InjectMocks
    lateinit var taskController: TaskController

    @Mock
    lateinit var taskService: TaskService

    @Nested
    @DisplayName("Tests for getting tasks operations in TaskController")
    inner class GetTasks {

        @Test
        fun `should get task by id`() {
            // GIVEN
            val taskId = LONG_ONE
            val task = TaskDtoForSingleWords(id = LONG_ONE, serialNumber = INTEGER_ONE, exerciseId = LONG_ONE)
            `when`(taskService.getTaskById(taskId)).thenReturn(task)
            // WHEN
            val actualResult: TaskDtoForSingleWords = taskController.getTaskById(taskId).body?.data as TaskDtoForSingleWords
            // THEN
            assertThat(actualResult).isEqualTo(task)
            verify(taskService).getTaskById(taskId)
        }

        @Test
        fun `should get tasks by exerciseId`() {
            // GIVEN
            val exerciseId = LONG_ONE
            val taskFirst = TaskDtoForSingleWords(id = LONG_ONE, serialNumber = INTEGER_ONE, exerciseId = LONG_ONE)
            val taskSecond = TaskDtoForSingleWords(id = 2L, serialNumber = INTEGER_TWO, exerciseId = LONG_ONE)
            `when`(taskService.getTasksByExerciseId(exerciseId)).thenReturn(listOf(taskFirst, taskSecond))
            // WHEN
            @Suppress("UNCHECKED_CAST")
            val actualResult: List<TaskDtoForSingleWords> =
                taskController.getTasksByExerciseId(exerciseId).body?.data as List<TaskDtoForSingleWords>
            // THEN
            assertThat(actualResult)
                .hasSize(INTEGER_TWO)
                .containsExactly(taskFirst, taskSecond)
            verify(taskService).getTasksByExerciseId(exerciseId)
        }
    }
}
