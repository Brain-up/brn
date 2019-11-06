package com.epam.brn.service

import com.epam.brn.dto.TaskDto
import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.model.Task
import com.epam.brn.repo.TaskRepository
import org.apache.commons.lang3.math.NumberUtils.LONG_ONE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional
import kotlin.test.assertFailsWith

@ExtendWith(
    MockitoExtension::class
)
@DisplayName("TaskService test using mockito")
internal class TaskServiceTest {

    @Mock
    lateinit var taskRepository: TaskRepository

    @InjectMocks
    lateinit var taskService: TaskService

    lateinit var testTask: Task
    lateinit var secondTestTask: Task

    @BeforeEach
    fun init() {
        testTask = Task(id = 1, name = "test_task", serialNumber = 12)
        secondTestTask = Task(id = 2, name = "second", serialNumber = 2)
    }

    @Nested
    @DisplayName("Tests for getting tasks with parameters")
    inner class GetTasks {
        @Test
        fun `should return all tasks with answers for certain exercise`() {

            `when`(taskRepository.findAllTasksByExerciseIdWithJoinedAnswers(LONG_ONE))
                .thenReturn(
                    listOf(testTask)
                )
            // WHEN
            val findAllTasksWithAnswers = taskService.getAllTasksByExerciseId(LONG_ONE)

            // THEN
            assertThat(findAllTasksWithAnswers)
                .usingElementComparatorOnFields("id", "name", "serialNumber")
                .containsExactly(TaskDto(id = testTask.id, name = testTask.name, serialNumber = testTask.serialNumber))
        }

        @Test
        fun `should return task by id`() {

            `when`(taskRepository.findById(LONG_ONE))
                .thenReturn(
                    Optional.of(testTask)
                )
            // WHEN
            val findAllTasksWithAnswers = taskService.getTaskById(LONG_ONE)

            // THEN
            assertThat(findAllTasksWithAnswers)
                .isEqualToComparingOnlyGivenFields(
                    TaskDto(id = testTask.id, name = testTask.name, serialNumber = testTask.serialNumber),
                    "id", "name", "serialNumber"
                )
        }

        @Test
        fun `should throw an exception when there is no task by specified id`() {

            `when`(taskRepository.findById(LONG_ONE))
                .thenReturn(
                    Optional.empty()
                )

            assertFailsWith<NoDataFoundException> {
                taskService.getTaskById(LONG_ONE)
            }
        }

        @Test
        fun `should return all tasks`() {

            `when`(taskRepository.findAllTasksByExerciseIdWithJoinedAnswers(LONG_ONE))
                .thenReturn(
                    listOf(testTask, secondTestTask)
                )
            // WHEN
            val findAllTasksWithAnswers = taskService.getAllTasksByExerciseId(LONG_ONE)

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