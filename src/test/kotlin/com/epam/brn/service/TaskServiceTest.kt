package com.epam.brn.service

import com.epam.brn.dto.WordsSeriesTaskDto
import com.epam.brn.enums.Locale
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.nhaarman.mockito_kotlin.verify
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
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@ExtendWith(MockitoExtension::class)
@DisplayName("TaskService test using mockito")
internal class TaskServiceTest {

    @Mock
    lateinit var taskRepository: TaskRepository

    @Mock
    lateinit var exerciseRepository: ExerciseRepository

    @Mock
    lateinit var resourceRepository: ResourceRepository

    @Mock
    lateinit var wordsService: WordsService

    @InjectMocks
    lateinit var taskService: TaskService

    @Nested
    @DisplayName("Tests for getting tasks with parameters")
    inner class GetTasks {
        @Test
        fun `should return tasks by exerciseId`() {
            // GIVEN
            val task1 = mock(Task::class.java)
            val taskDto1 = mock(WordsSeriesTaskDto::class.java)
            val task2 = mock(Task::class.java)
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            val taskDto2 = mock(WordsSeriesTaskDto::class.java)
            val exercise = mock(Exercise::class.java)
            val subGroup = mock(SubGroup::class.java)
            val series = mock(Series::class.java)
            `when`(taskRepository.findTasksByExerciseIdWithJoinedAnswers(LONG_ONE))
                .thenReturn(listOf(task1, task2))
            `when`(exerciseRepository.findById(LONG_ONE))
                .thenReturn(Optional.of(exercise))
            `when`(taskRepository.findTasksByExerciseIdWithJoinedAnswers(LONG_ONE))
                .thenReturn(listOf(task1, task2))

            `when`(exercise.subGroup).thenReturn(subGroup)
            `when`(subGroup.series).thenReturn(series)
            `when`(series.type).thenReturn(ExerciseType.SINGLE_SIMPLE_WORDS.name)
            `when`(task1.toWordsSeriesTaskDto()).thenReturn(taskDto1)
            `when`(task2.toWordsSeriesTaskDto()).thenReturn(taskDto2)
            `when`(task1.answerOptions).thenReturn(mutableSetOf(resource))

            `when`(wordsService.getFullS3UrlForWord(resource.word, resource.locale)).thenReturn("fullUrl")
            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)
            // THEN
            assertEquals(2, foundTasks.size)
            verify(wordsService).getFullS3UrlForWord(resource.word, resource.locale)
        }

        @Test
        fun `should return task by id`() {
            // GIVEN
            val task = mock(Task::class.java)
            val exercise = mock(Exercise::class.java)
            val subGroup = mock(SubGroup::class.java)
            val series = mock(Series::class.java)
            val taskDto = WordsSeriesTaskDto(id = 1L, exerciseType = ExerciseType.SINGLE_SIMPLE_WORDS)
            `when`(taskRepository.findById(LONG_ONE))
                .thenReturn(Optional.of(task))
            `when`(task.exercise).thenReturn(exercise)
            `when`(exercise.subGroup).thenReturn(subGroup)
            `when`(subGroup.series).thenReturn(series)
            `when`(series.type).thenReturn(ExerciseType.SINGLE_SIMPLE_WORDS.name)
            `when`(task.toWordsSeriesTaskDto()).thenReturn(taskDto)
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
