package com.epam.brn.controller

import com.epam.brn.dto.WordsSeriesTaskResponse
import com.epam.brn.model.ExerciseType
import com.epam.brn.service.TaskService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE
import org.apache.commons.lang3.math.NumberUtils.INTEGER_TWO
import org.apache.commons.lang3.math.NumberUtils.LONG_ONE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class TaskControllerTest {

    @InjectMockKs
    lateinit var taskController: TaskController

    @MockK
    lateinit var taskService: TaskService

    @Nested
    @DisplayName("Tests for getting tasks operations in TaskController")
    inner class GetTasks {

        @Test
        fun `should get task by id`() {
            // GIVEN
            val taskId = LONG_ONE
            val task = WordsSeriesTaskResponse(
                id = LONG_ONE,
                serialNumber = INTEGER_ONE,
                exerciseId = LONG_ONE,
                exerciseType = ExerciseType.SINGLE_SIMPLE_WORDS
            )
            every { taskService.getTaskById(taskId) } returns task

            // WHEN
            val actualResult: WordsSeriesTaskResponse = taskController.getTaskById(taskId).body?.data as WordsSeriesTaskResponse

            // THEN
            verify(exactly = 1) { taskService.getTaskById(taskId) }
            assertThat(actualResult).isEqualTo(task)
        }

        @Test
        fun `should get tasks by exerciseId`() {
            // GIVEN
            val exerciseId = LONG_ONE
            val taskFirst = WordsSeriesTaskResponse(
                id = LONG_ONE,
                serialNumber = INTEGER_ONE,
                exerciseId = LONG_ONE,
                exerciseType = ExerciseType.SINGLE_SIMPLE_WORDS
            )
            val taskSecond = WordsSeriesTaskResponse(
                id = 2L,
                serialNumber = INTEGER_TWO,
                exerciseId = LONG_ONE,
                exerciseType = ExerciseType.SINGLE_SIMPLE_WORDS
            )
            every { taskService.getTasksByExerciseId(exerciseId) } returns listOf(taskFirst, taskSecond)

            // WHEN
            @Suppress("UNCHECKED_CAST")
            val actualResult: List<WordsSeriesTaskResponse> =
                taskController.getTasksByExerciseId(exerciseId).body?.data as List<WordsSeriesTaskResponse>

            // THEN
            verify(exactly = 1) { taskService.getTasksByExerciseId(exerciseId) }
            assertThat(actualResult)
                .hasSize(INTEGER_TWO)
                .containsExactly(taskFirst, taskSecond)
        }
    }
}
