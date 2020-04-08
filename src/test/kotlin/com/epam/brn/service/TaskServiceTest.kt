package com.epam.brn.service

import com.epam.brn.dto.`TaskDtoFor1Series`
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.TaskRepository
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import org.apache.commons.lang3.math.NumberUtils.LONG_ONE
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(
    MockitoExtension::class
)
@DisplayName("TaskService test using mockito")
internal class TaskServiceTest {

    @Mock
    lateinit var taskRepository: TaskRepository

    @Mock
    lateinit var exerciseRepository: ExerciseRepository

    @Mock
    lateinit var resourceRepository: ResourceRepository

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
            `when`(exercise.exerciseType).thenReturn(ExerciseType.SINGLE_WORDS.toString())
            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)
            // THEN
            assertEquals(2, foundTasks.size)
        }

        @Test
        fun `should return task by id`() {
            // GIVEN
            val task = mock(Task::class.java)
            val exercise = mock(Exercise::class.java)
            val taskDto = `TaskDtoFor1Series`()
            `when`(taskRepository.findById(LONG_ONE))
                .thenReturn(Optional.of(task))
            `when`(task.exercise).thenReturn(exercise)
            `when`(task.to1SeriesTaskDto()).thenReturn(taskDto)
            `when`(exercise.exerciseType).thenReturn(ExerciseType.SINGLE_WORDS.toString())
            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)
            // THEN
            assertSame(taskDto, taskById)
        }

        @Test
        fun `should throw an exception when there is no task by specified id`() {
            // GIVEN
            `when`(taskRepository.findById(LONG_ONE))
                .thenReturn(Optional.empty())
            // THEN
            assertFailsWith<EntityNotFoundException> {
                taskService.getTaskById(LONG_ONE)
            }
        }
    }
}
