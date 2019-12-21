package com.epam.brn.service

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.dto.TaskDtoForSingleWords
import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
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
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(
    MockitoExtension::class
)
@DisplayName("TaskService test using mockito")
internal class TaskServiceTest {

    @Mock
    lateinit var taskRepository: TaskRepository

    @Mock
    lateinit var exerciseRepository: ExerciseRepository

    @InjectMocks
    lateinit var taskService: TaskService

    @Nested
    @DisplayName("Tests for getting tasks with parameters")
    inner class GetTasks {
        @Test
        fun `should return tasks by exerciseId`() {
            // GIVEN
            val exercise = mock(Exercise::class.java)
            val task1 = mock(Task::class.java)
            val task2 = mock(Task::class.java)

            `when`(taskRepository.findTasksByExerciseIdWithJoinedAnswers(LONG_ONE))
                .thenReturn(listOf(task1, task2))
            `when`(exerciseRepository.findById(LONG_ONE))
                .thenReturn(Optional.of(exercise))

            `when`(exercise.exerciseType).thenReturn(ExerciseTypeEnum.SINGLE_WORDS)

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            assertEquals(2, foundTasks.size)
        }

        @Test
        fun `should return task by id`() {
            // GIVEN
            val testTask = Task(id = 1, name = "test_task", serialNumber = 12)
            `when`(taskRepository.findById(LONG_ONE))
                .thenReturn(Optional.of(testTask))
            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)
            // THEN
            assertThat(taskById)
                .isEqualToComparingOnlyGivenFields(
                    TaskDtoForSingleWords(id = testTask.id, name = testTask.name, serialNumber = testTask.serialNumber),
                    "id", "name", "serialNumber"
                )
        }

        @Test
        fun `should throw an exception when there is no task by specified id`() {
            // GIVEN
            `when`(taskRepository.findById(LONG_ONE))
                .thenReturn(Optional.empty())
            // THEN
            assertFailsWith<NoDataFoundException> {
                taskService.getTaskById(LONG_ONE)
            }
        }
    }
}