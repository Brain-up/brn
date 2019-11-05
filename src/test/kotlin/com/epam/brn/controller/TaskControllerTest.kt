package com.epam.brn.controller

import com.epam.brn.dto.TaskDto
import com.epam.brn.job.csv.task.impl.UploadTaskFromCsvJob
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

    @Mock
    lateinit var uploadTaskFromCsvJob: UploadTaskFromCsvJob

    @Nested
    @DisplayName("Tests for getting tasks operations in TaskController")
    inner class GetTasks {

        @Test
        fun `should get task by id`() {
            // GIVEN
            val taskId = LONG_ONE
            val task = TaskDto(id = LONG_ONE, serialNumber = INTEGER_ONE, exercise = LONG_ONE)
            `when`(taskService.getTaskById(taskId)).thenReturn(task)
            // WHEN
            val actualResult: TaskDto = taskController.getTaskById(taskId).body?.data as TaskDto
            // THEN
            assertThat(actualResult).isEqualTo(task)
            verify(taskService).getTaskById(taskId)
        }

        @Test
        fun `should get tasks by exerciseId`() {
            // GIVEN
            val exerciseId = LONG_ONE
            val taskFirst = TaskDto(id = LONG_ONE, serialNumber = INTEGER_ONE, exercise = LONG_ONE)
            val taskSecond = TaskDto(id = 2L, serialNumber = INTEGER_TWO, exercise = LONG_ONE)
            `when`(taskService.getAllTasksByExerciseId(exerciseId)).thenReturn(listOf(taskFirst, taskSecond))
            // WHEN
            @Suppress("UNCHECKED_CAST")
            val actualResult: List<TaskDto> =
                taskController.getTasksByExerciseId(exerciseId).body?.data as List<TaskDto>
            // THEN
            assertThat(actualResult)
                .hasSize(INTEGER_TWO)
                .containsExactly(taskFirst, taskSecond)
            verify(taskService).getAllTasksByExerciseId(exerciseId)
        }
    }
}
