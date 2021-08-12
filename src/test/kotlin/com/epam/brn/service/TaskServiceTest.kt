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
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.math.NumberUtils.LONG_ONE
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@ExtendWith(MockKExtension::class)
@DisplayName("TaskService test using MockK")
internal class TaskServiceTest {

    @InjectMockKs
    lateinit var taskService: TaskService

    @MockK
    lateinit var taskRepository: TaskRepository

    @MockK
    lateinit var exerciseRepository: ExerciseRepository

    @MockK
    lateinit var resourceRepository: ResourceRepository

    @MockK
    lateinit var wordsService: WordsService

    @Nested
    @DisplayName("Tests for getting tasks with parameters")
    inner class GetTasks {
        @Test
        fun `should return tasks by exerciseId`() {
            // GIVEN
            val task1 = mockk<Task>()
            val taskDto1 = mockk<WordsSeriesTaskDto>()
            val task2 = mockk<Task>()
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            val taskDto2 = mockk<WordsSeriesTaskDto>()
            val exercise = mockk<Exercise>()
            val subGroup = mockk<SubGroup>()
            val series = mockk<Series>()
            every { taskRepository.findTasksByExerciseIdWithJoinedAnswers(LONG_ONE) } returns listOf(task1, task2)
            every { exerciseRepository.findById(LONG_ONE) } returns Optional.of(exercise)
            every { taskRepository.findTasksByExerciseIdWithJoinedAnswers(LONG_ONE) } returns listOf(task1, task2)

            every { exercise.subGroup } returns subGroup
            every { subGroup.series } returns series
            every { series.type } returns ExerciseType.SINGLE_SIMPLE_WORDS.name
            every { task1.toWordsSeriesTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto1
            every { task2.toWordsSeriesTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto2
            every { task1.answerOptions } returns mutableSetOf(resource)
            every { task2.answerOptions } returns mutableSetOf(resource)

            every { wordsService.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            verify(exactly = 2) { wordsService.getFullS3UrlForWord(resource.word, resource.locale) }
            assertEquals(2, foundTasks.size)
        }

        @Test
        fun `should return task by id`() {
            // GIVEN
            val task = mockk<Task>()
            val exercise = mockk<Exercise>()
            val subGroup = mockk<SubGroup>()
            val series = mockk<Series>()
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            val taskDto = WordsSeriesTaskDto(id = 1L, exerciseType = ExerciseType.SINGLE_SIMPLE_WORDS)
            every { taskRepository.findById(LONG_ONE) } returns Optional.of(task)
            every { task.answerOptions } returns mutableSetOf(resource)
            every { wordsService.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"
            every { task.exercise } returns exercise
            every { exercise.subGroup } returns subGroup
            every { subGroup.series } returns series
            every { series.type } returns ExerciseType.SINGLE_SIMPLE_WORDS.name
            every { task.toWordsSeriesTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            assertSame(taskDto, taskById)
        }

        @Test
        fun `should throw an exception when there is no task by specified id`() {
            // GIVEN
            every { taskRepository.findById(LONG_ONE) } returns Optional.empty()

            // THEN
            assertFailsWith<EntityNotFoundException> {
                taskService.getTaskById(LONG_ONE)
            }
        }
    }
}
