package com.epam.brn.service

import com.epam.brn.dto.WordsGroupSeriesTaskDto
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
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
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
    lateinit var resource: Resource

    @MockK
    lateinit var taskDto1: WordsSeriesTaskDto

    @MockK
    lateinit var taskDto2: WordsSeriesTaskDto

    @MockK
    lateinit var wordsGroupSeriesTaskDto1: WordsGroupSeriesTaskDto

    @MockK
    lateinit var wordsGroupSeriesTaskDto2: WordsGroupSeriesTaskDto

    @MockK
    lateinit var exercise: Exercise

    @MockK
    lateinit var exerciseType: ExerciseType

    @MockK
    lateinit var subGroup: SubGroup

    @MockK
    lateinit var series: Series

    @Nested
    @DisplayName("Tests for getting tasks with parameters")
    inner class GetTasks {
        @Test
        fun `should return tasks by exerciseId(SINGLE_SIMPLE_WORDS)`() {
            // GIVEN
            val expectedTaskSize = 2
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
            foundTasks.size.shouldBe(expectedTaskSize)
        }

        @Test
        fun `should return tasks by exerciseId(WORDS_SEQUENCES)`() {
            // GIVEN
            val expectedTaskSize = 2
            val template = ""
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            every { taskRepository.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1,
                task2
            )
            every { exerciseRepository.findById(ofType(Long::class)) } returns Optional.of(exercise)

            every { task1.answerOptions } returns mutableSetOf(resource)
            every { task2.answerOptions } returns mutableSetOf()
            every { task1.exercise } returns exercise
            every { task2.exercise } returns exercise
            every { exercise.template } returns template
            every { task1.toWordsGroupSeriesTaskDto(template) } returns wordsGroupSeriesTaskDto1
            every { task2.toWordsGroupSeriesTaskDto(template) } returns wordsGroupSeriesTaskDto2

            every { exercise.subGroup } returns subGroup
            every { subGroup.series } returns series
            every { series.type } returns ExerciseType.WORDS_SEQUENCES.name

            every { wordsService.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            verify(exactly = 1) { wordsService.getFullS3UrlForWord(resource.word, resource.locale) }
            foundTasks.size.shouldBe(expectedTaskSize)
        }

        @Test
        fun `should return tasks by exerciseId(SENTENCE)`() {
            // GIVEN
            val expectedTaskSize = 2
            val template = ""
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            every { taskRepository.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1,
                task2
            )
            every { exerciseRepository.findById(ofType(Long::class)) } returns Optional.of(exercise)

            every { task1.answerOptions } returns mutableSetOf(resource)
            every { task2.answerOptions } returns mutableSetOf()
            every { task1.exercise } returns exercise
            every { task2.exercise } returns exercise
            every { exercise.template } returns template
            every { task1.toSentenceSeriesTaskDto(template) } returns wordsGroupSeriesTaskDto1
            every { task2.toSentenceSeriesTaskDto(template) } returns wordsGroupSeriesTaskDto2

            every { exercise.subGroup } returns subGroup
            every { subGroup.series } returns series
            every { series.type } returns ExerciseType.SENTENCE.name

            every { wordsService.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            verify(exactly = 1) { wordsService.getFullS3UrlForWord(resource.word, resource.locale) }
            foundTasks.size.shouldBe(expectedTaskSize)
        }

        @Test
        fun `should return tasks by exerciseId(PHRASES)`() {
            // GIVEN
            val expectedTaskSize = 2
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            every { taskRepository.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1,
                task2
            )
            every { exerciseRepository.findById(ofType(Long::class)) } returns Optional.of(exercise)

            every { task1.answerOptions } returns mutableSetOf(resource)
            every { task2.answerOptions } returns mutableSetOf()
            every { task1.to4SeriesTaskDto() } returns taskDto1
            every { task2.to4SeriesTaskDto() } returns taskDto2

            every { exercise.subGroup } returns subGroup
            every { subGroup.series } returns series
            every { series.type } returns ExerciseType.PHRASES.name

            every { wordsService.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            verify(exactly = 1) { wordsService.getFullS3UrlForWord(resource.word, resource.locale) }
            foundTasks.size.shouldBe(expectedTaskSize)
        }

        @Test
        fun `should throw an exception when there is no task for this signal exercise type`() {
            // GIVEN
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            every { taskRepository.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1,
                task2
            )
            every { exerciseRepository.findById(ofType(Long::class)) } returns Optional.of(exercise)

            every { task1.answerOptions } returns mutableSetOf(resource)
            every { task2.answerOptions } returns mutableSetOf()
            every { task1.exercise } returns exercise
            every { task2.exercise } returns exercise

            every { exercise.subGroup } returns subGroup
            every { subGroup.series } returns series
            every { series.type } returns ExerciseType.DI.name

            every { wordsService.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"

            // THEN
            shouldThrowExactly<EntityNotFoundException> {
                taskService.getTasksByExerciseId(LONG_ONE)
            }
        }

        @Test
        fun `should return task by id(SINGLE_SIMPLE_WORDS)`() {
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
            taskById.shouldBe(taskDto)
        }

        @Test
        fun `should return task by id(WORDS_SEQUENCES)`() {
            // GIVEN
            val template = ""
            every { taskRepository.findById(ofType(Long::class)) } returns Optional.of(task1)

            every { task1.answerOptions } returns mutableSetOf()

            every { task1.exercise } returns exercise
            every { exercise.subGroup } returns subGroup
            every { subGroup.series } returns series
            every { task1.id } returns 1L
            every { series.type } returns ExerciseType.WORDS_SEQUENCES.name
            every { exercise.template } returns template
            every { task1.toWordsGroupSeriesTaskDto(template) } returns wordsGroupSeriesTaskDto1

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            assertSame(wordsGroupSeriesTaskDto1, taskById)
        }

        @Test
        fun `should return task by id(SENTENCE)`() {
            // GIVEN
            val template = ""
            every { taskRepository.findById(ofType(Long::class)) } returns Optional.of(task1)

            every { task1.answerOptions } returns mutableSetOf()

            every { task1.exercise } returns exercise
            every { exercise.subGroup } returns subGroup
            every { subGroup.series } returns series
            every { task1.id } returns 1L
            every { series.type } returns ExerciseType.SENTENCE.name
            every { exercise.template } returns template
            every { task1.toSentenceSeriesTaskDto(template) } returns wordsGroupSeriesTaskDto1

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            taskById.shouldBe(wordsGroupSeriesTaskDto1)
        }

        @Test
        fun `should return task by id(PHRASES)`() {
            // GIVEN
            val taskDto = WordsSeriesTaskDto(id = 1L, exerciseType = ExerciseType.PHRASES)
            every { taskRepository.findById(ofType(Long::class)) } returns Optional.of(task1)
            every { task1.answerOptions } returns mutableSetOf()
            every { task1.exercise } returns exercise
            every { exercise.subGroup } returns subGroup
            every { subGroup.series } returns series
            every { series.type } returns ExerciseType.PHRASES.name
            every { task1.to4SeriesTaskDto() } returns taskDto

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            taskById.shouldBe(taskDto)
        }

        @Test
        fun `should throw an exception when there is no tasks for this exercise type`() {
            // GIVEN
            every { taskRepository.findById(ofType(Long::class)) } returns Optional.of(task1)
            every { task1.answerOptions } returns mutableSetOf()
            every { task1.exercise } returns exercise
            every { exercise.subGroup } returns subGroup
            every { subGroup.series } returns series
            every { series.type } returns ExerciseType.DI.name

            // THEN
            shouldThrowExactly<EntityNotFoundException> {
                taskService.getTaskById(LONG_ONE)
            }
        }

        @Test
        fun `should throw an exception when there is no task by exercise id`() {
            // GIVEN
            every { exerciseRepository.findById(LONG_ONE) } returns Optional.empty()

            // THEN
            shouldThrowExactly<EntityNotFoundException> {
                taskService.getTasksByExerciseId(LONG_ONE)
            }
        }

        @Test
        fun `should throw an exception when there is no task by specified id`() {
            // GIVEN
            every { taskRepository.findById(LONG_ONE) } returns Optional.empty()

            // THEN
            shouldThrowExactly<EntityNotFoundException> {
                taskService.getTaskById(LONG_ONE)
            }
        }

        @Test
        fun `should save task`() {
            // GIVEN
            val resources = mutableSetOf<Resource>()
            every { task1.answerOptions } returns mutableSetOf()
            every { task1.correctAnswer } returns resource
            every { taskRepository.save(task1) } returns task1
            every { resourceRepository.saveAll(any()) } returns resources

            // WHEN
            val task = taskService.save(task1)

            // THEN
            task.shouldBe(task1)
        }
    }
}
