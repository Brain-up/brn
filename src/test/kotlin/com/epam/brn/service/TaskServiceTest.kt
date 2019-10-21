package com.epam.brn.service

import com.epam.brn.dto.TaskDto
import com.epam.brn.model.Task
import com.epam.brn.repo.TaskRepository
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

@ExtendWith(
    MockitoExtension::class
)
@DisplayName("Taskservice test using mockito")
internal class TaskServiceTest {

    @Mock
    lateinit var taskRepository: TaskRepository

    @InjectMocks
    lateinit var taskService: TaskService

    @Nested
    @DisplayName("Tests for getting tasks with parameters")
    inner class GetTasks {
        @Test
        fun `should return all tasks with answers for certain exercise`() {

            // GIVEN
            val testTask = Task(id = 123, name = "test_task", serialNumber = 12)

            `when`(taskRepository.findAllTasksByExerciseIdWithJoinedAnswers(LONG_ONE))
                .thenReturn(
                    listOf(testTask)
                )
            // WHEN
            val findAllTasksWithAnswers = taskService.findAllTasksByExerciseIdIncludeAnswers(LONG_ONE)

            // THEN
            assertThat(findAllTasksWithAnswers)
                .usingElementComparatorOnFields("id", "name", "serialNumber")
                .containsExactly(TaskDto(id = testTask.id, name = testTask.name, serialNumber = testTask.serialNumber))
        }

        @Test
        fun `should return all tasks`() {

            // GIVEN
            val testTask = Task(id = 123, name = "test_task", serialNumber = 12)
            val secondTestTask = Task(id = 2, name = "second", serialNumber = 2)

            `when`(taskRepository.findAllTasksByExerciseIdWithJoinedAnswers(LONG_ONE))
                .thenReturn(
                    listOf(testTask, secondTestTask)
                )
            // WHEN
            val findAllTasksWithAnswers = taskService.findAllTasksByExerciseIdIncludeAnswers(LONG_ONE)

            // THEN
            assertThat(findAllTasksWithAnswers)
                .usingElementComparatorOnFields("id", "name", "serialNumber")
                .containsExactly(
                    TaskDto(id = testTask.id, name = testTask.name, serialNumber = testTask.serialNumber),
                    TaskDto(
                        id = secondTestTask.id,
                        name = secondTestTask.name,
                        serialNumber = secondTestTask.serialNumber
                    )
                )
        }
    }
}