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
@DisplayName("TaskService test using MockK")
internal class TaskServiceTest {

    @InjectMockKs
    lateinit var taskService: TaskService

    @MockK
    lateinit var taskRepositoryMock: TaskRepository

    @MockK
    lateinit var exerciseRepositoryMock: ExerciseRepository

    @MockK
    lateinit var resourceRepositoryMock: ResourceRepository

    @MockK
    lateinit var wordsServiceMock: WordsService

    @MockK
    lateinit var task1Mock: Task

    @MockK
    lateinit var task2Mock: Task

    @MockK
    lateinit var resourceMock: Resource

    @MockK
    lateinit var taskDto1Mock: WordsSeriesTaskDto

    @MockK
    lateinit var taskDto2Mock: WordsSeriesTaskDto

    @MockK
    lateinit var wordsGroupSeriesTaskDto1Mock: WordsGroupSeriesTaskDto

    @MockK
    lateinit var wordsGroupSeriesTaskDto2Mock: WordsGroupSeriesTaskDto

    @MockK
    lateinit var exerciseMock: Exercise

    @MockK
    lateinit var subGroupMock: SubGroup

    @MockK
    lateinit var seriesMock: Series

    @Nested
    @DisplayName("Tests for getting tasks with parameters")
    inner class GetTasks {
        @Test
        fun `should return tasks by exerciseId(SINGLE_SIMPLE_WORDS)`() {
            // GIVEN
            val expectedTaskSize = 2
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            every { taskRepositoryMock.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1Mock,
                task2Mock
            )
            every { exerciseRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(exerciseMock)

            every { task1Mock.answerOptions } returns mutableSetOf(resource)
            every { task2Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.toWordsSeriesTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto1Mock
            every { task2Mock.toWordsSeriesTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto2Mock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.SINGLE_SIMPLE_WORDS.name

            every { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            verify(exactly = 1) { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) }
            foundTasks.size shouldBe expectedTaskSize
        }

        @Test
        fun `should return tasks by exerciseId(WORDS_SEQUENCES)`() {
            // GIVEN
            val expectedTaskSize = 2
            val template = ""
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            every { taskRepositoryMock.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1Mock,
                task2Mock
            )
            every { exerciseRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(exerciseMock)

            every { task1Mock.answerOptions } returns mutableSetOf(resource)
            every { task2Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.exercise } returns exerciseMock
            every { task2Mock.exercise } returns exerciseMock
            every { exerciseMock.template } returns template
            every { task1Mock.toWordsGroupSeriesTaskDto(template) } returns wordsGroupSeriesTaskDto1Mock
            every { task2Mock.toWordsGroupSeriesTaskDto(template) } returns wordsGroupSeriesTaskDto2Mock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.WORDS_SEQUENCES.name

            every { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            verify(exactly = 1) { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) }
            foundTasks.size shouldBe expectedTaskSize
        }

        @Test
        fun `should return tasks by exerciseId(SENTENCE)`() {
            // GIVEN
            val expectedTaskSize = 2
            val template = ""
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            every { taskRepositoryMock.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1Mock,
                task2Mock
            )
            every { exerciseRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(exerciseMock)

            every { task1Mock.answerOptions } returns mutableSetOf(resource)
            every { task2Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.exercise } returns exerciseMock
            every { task2Mock.exercise } returns exerciseMock
            every { exerciseMock.template } returns template
            every { task1Mock.toSentenceSeriesTaskDto(template) } returns wordsGroupSeriesTaskDto1Mock
            every { task2Mock.toSentenceSeriesTaskDto(template) } returns wordsGroupSeriesTaskDto2Mock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.SENTENCE.name

            every { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            verify(exactly = 1) { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) }
            foundTasks.size shouldBe expectedTaskSize
        }

        @Test
        fun `should return tasks by exerciseId(PHRASES)`() {
            // GIVEN
            val expectedTaskSize = 2
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            every { taskRepositoryMock.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1Mock,
                task2Mock
            )
            every { exerciseRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(exerciseMock)

            every { task1Mock.toWordsSeriesTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto1Mock
            every { task2Mock.toWordsSeriesTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto2Mock
            every { task1Mock.answerOptions } returns mutableSetOf(resource)
            every { task2Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.toPhraseSeriesTaskDto() } returns taskDto1Mock
            every { task2Mock.toPhraseSeriesTaskDto() } returns taskDto2Mock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.PHRASES.name

            every { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            verify(exactly = 1) { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) }
            foundTasks.size shouldBe expectedTaskSize
        }

        @Test
        fun `should throw an exception when there is no task for this signal exercise type`() {
            // GIVEN
            val resource = Resource(word = "word", locale = Locale.RU.locale)
            every { taskRepositoryMock.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1Mock,
                task2Mock
            )
            every { exerciseRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(exerciseMock)

            every { task1Mock.answerOptions } returns mutableSetOf(resource)
            every { task2Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.exercise } returns exerciseMock
            every { task2Mock.exercise } returns exerciseMock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.DI.name

            every { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"

            // THEN
            shouldThrowExactly<EntityNotFoundException> {
                taskService.getTasksByExerciseId(LONG_ONE)
            }
        }

        @Test
        fun `should return task by id(SINGLE_SIMPLE_WORDS)`() {
            // GIVEN
            val taskDto = WordsSeriesTaskDto(id = 1L, exerciseType = ExerciseType.SINGLE_SIMPLE_WORDS)
            every { taskRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(task1Mock)
            every { task1Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.exercise } returns exerciseMock
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.SINGLE_SIMPLE_WORDS.name
            every { task1Mock.toWordsSeriesTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            taskById shouldBe taskDto
        }

        @Test
        fun `should return task by id(WORDS_SEQUENCES)`() {
            // GIVEN
            val template = ""
            every { taskRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(task1Mock)

            every { task1Mock.answerOptions } returns mutableSetOf()

            every { task1Mock.exercise } returns exerciseMock
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { task1Mock.id } returns 1L
            every { seriesMock.type } returns ExerciseType.WORDS_SEQUENCES.name
            every { exerciseMock.template } returns template
            every { task1Mock.toWordsGroupSeriesTaskDto(template) } returns wordsGroupSeriesTaskDto1Mock

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            assertSame(wordsGroupSeriesTaskDto1Mock, taskById)
        }

        @Test
        fun `should return task by id(SENTENCE)`() {
            // GIVEN
            val template = ""
            every { taskRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(task1Mock)

            every { task1Mock.answerOptions } returns mutableSetOf()

            every { task1Mock.exercise } returns exerciseMock
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { task1Mock.id } returns 1L
            every { seriesMock.type } returns ExerciseType.SENTENCE.name
            every { exerciseMock.template } returns template
            every { task1Mock.toSentenceSeriesTaskDto(template) } returns wordsGroupSeriesTaskDto1Mock

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            taskById shouldBe wordsGroupSeriesTaskDto1Mock
        }

        @Test
        fun `should return task by id(PHRASES)`() {
            // GIVEN
            val taskDto = WordsSeriesTaskDto(id = 1L, exerciseType = ExerciseType.PHRASES)
            every { taskRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(task1Mock)
            every { task1Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.exercise } returns exerciseMock
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.PHRASES.name
            every { task1Mock.toPhraseSeriesTaskDto() } returns taskDto

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            taskById shouldBe taskDto
        }

        @Test
        fun `should throw an exception when there is no tasks for this exercise type`() {
            // GIVEN
            every { taskRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(task1Mock)
            every { task1Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.exercise } returns exerciseMock
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.DI.name

            // THEN
            shouldThrowExactly<EntityNotFoundException> {
                taskService.getTaskById(LONG_ONE)
            }
        }

        @Test
        fun `should throw an exception when there is no task by exercise id`() {
            // GIVEN
            every { exerciseRepositoryMock.findById(LONG_ONE) } returns Optional.empty()

            // THEN
            shouldThrowExactly<EntityNotFoundException> {
                taskService.getTasksByExerciseId(LONG_ONE)
            }
        }

        @Test
        fun `should throw an exception when there is no task by specified id`() {
            // GIVEN
            every { taskRepositoryMock.findById(LONG_ONE) } returns Optional.empty()

            // THEN
            shouldThrowExactly<EntityNotFoundException> {
                taskService.getTaskById(LONG_ONE)
            }
        }

        @Test
        fun `should save task`() {
            // GIVEN
            val resources = mutableSetOf<Resource>()
            every { task1Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.correctAnswer } returns resourceMock
            every { taskRepositoryMock.save(task1Mock) } returns task1Mock
            every { resourceRepositoryMock.saveAll(any()) } returns resources

            // WHEN
            val task = taskService.save(task1Mock)

            // THEN
            task shouldBe task1Mock
        }
    }
}
