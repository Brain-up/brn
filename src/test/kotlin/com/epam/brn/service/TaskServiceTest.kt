package com.epam.brn.service

import com.epam.brn.dto.WordsSeriesTaskDto
import com.epam.brn.enums.Locale
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.TaskRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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
@DisplayName("TaskService test using mockito")
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

    @MockK
    lateinit var task1: Task

    @MockK
    lateinit var task2: Task

    @MockK
    lateinit var taskDto1: WordsSeriesTaskDto

    @MockK
    lateinit var taskDto2: WordsSeriesTaskDto

    @MockK
    lateinit var exercise: Exercise

    @MockK
    lateinit var subGroup: SubGroup

    @MockK
    lateinit var series: Series

    @Nested
    @DisplayName("Tests for getting tasks with parameters")
    inner class GetTasks {
        @Test
        fun `should return tasks by exerciseId`() {
            // GIVEN
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            every { taskRepository.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1,
                task2
            )
            every { exerciseRepository.findById(ofType(Long::class)) } returns Optional.of(exercise)

            every { task1.answerOptions } returns mutableSetOf(resource)
            every { task2.answerOptions } returns mutableSetOf()
            every { task1.toWordsSeriesTaskDto() } returns taskDto1
            every { task2.toWordsSeriesTaskDto() } returns taskDto2

            every { exercise.subGroup } returns subGroup
            every { subGroup.series } returns series
            every { series.type } returns ExerciseType.SINGLE_SIMPLE_WORDS.name

            every { wordsService.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            verify(exactly = 1) { wordsService.getFullS3UrlForWord(resource.word, resource.locale) }
            assertEquals(2, foundTasks.size)
        }

        @Test
        fun `should return task by id`() {
            // GIVEN
            val taskDto = WordsSeriesTaskDto(id = 1L, exerciseType = ExerciseType.SINGLE_SIMPLE_WORDS)
            every { taskRepository.findById(ofType(Long::class)) } returns Optional.of(task1)
            every { task1.answerOptions } returns mutableSetOf()
            every { task1.exercise } returns exercise
            every { exercise.subGroup } returns subGroup
            every { subGroup.series } returns series
            every { series.type } returns ExerciseType.SINGLE_SIMPLE_WORDS.name
            every { task1.toWordsSeriesTaskDto() } returns taskDto

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
