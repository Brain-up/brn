package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.response.TaskResponse
import com.epam.brn.dto.response.TaskWordsGroupResponse
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.ExerciseMechanism
import com.epam.brn.enums.ExerciseType
import com.epam.brn.enums.WordType
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.service.cloud.CloudService
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.apache.commons.lang3.math.NumberUtils.LONG_ONE
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
    lateinit var cloudService: CloudService

    @MockK
    lateinit var task1Mock: Task

    @MockK
    lateinit var task2Mock: Task

    @MockK
    lateinit var resourceMock: Resource

    @MockK
    lateinit var taskDto1Mock: TaskResponse

    @MockK
    lateinit var taskDto2Mock: TaskResponse

    @MockK
    lateinit var taskWordsGroupResponse1Mock: TaskWordsGroupResponse

    @MockK
    lateinit var taskWordsGroupResponse2Mock: TaskWordsGroupResponse

    @MockK
    lateinit var exerciseMock: Exercise

    @MockK
    lateinit var exerciseDtoMock: ExerciseDto

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
            val resource = Resource(word = "word", locale = BrnLocale.RU.locale, pictureFileUrl = "fileUrl")
            every { taskRepositoryMock.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1Mock,
                task2Mock
            )
            every { exerciseRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(exerciseMock)

            every { task1Mock.answerOptions } returns mutableSetOf(resource)
            every { task2Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.toTaskResponse(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto1Mock
            every { task2Mock.toTaskResponse(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto2Mock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.SINGLE_SIMPLE_WORDS.name

            val baseFileUrl = "baseFileUrl"
            every { cloudService.baseFileUrl() } returns (baseFileUrl)

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            foundTasks.size shouldBe expectedTaskSize
        }

        @Test
        fun `should return tasks by exerciseId(WORDS_SEQUENCES)`() {
            // GIVEN
            val expectedTaskSize = 2
            val template = ""
            val resource = Resource(word = "word", locale = BrnLocale.RU.locale)
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
            every {
                task1Mock.toWordsGroupSeriesTaskDto(
                    ExerciseType.WORDS_SEQUENCES,
                    template
                )
            } returns taskWordsGroupResponse1Mock
            every {
                task2Mock.toWordsGroupSeriesTaskDto(
                    ExerciseType.WORDS_SEQUENCES,
                    template
                )
            } returns taskWordsGroupResponse2Mock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.WORDS_SEQUENCES.name

            val baseFileUrl = "baseFileUrl"
            every { cloudService.baseFileUrl() } returns (baseFileUrl)

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            foundTasks.size shouldBe expectedTaskSize
        }

        @Test
        fun `should return tasks by exerciseId(SINGLE_WORDS_KOROLEVA)`() {
            // GIVEN
            val template = ""
            val resource1 = Resource(word = "мак", locale = BrnLocale.RU.locale, wordType = WordType.OBJECT.name)
            val resource2 = Resource(word = "маки", locale = BrnLocale.RU.locale, wordType = WordType.OBJECT.name)
            every { taskRepositoryMock.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns
                listOf(task1Mock)
            every { exerciseRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(exerciseMock)
            every { task1Mock.answerOptions } returns mutableSetOf(resource1, resource2)
            every { task1Mock.exercise } returns exerciseMock
            every { task1Mock.id } returns 1
            every { task1Mock.name } returns "name"
            every { task1Mock.serialNumber } returns 2

            every { exerciseMock.template } returns template
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.SINGLE_WORDS_KOROLEVA.name

            val baseFileUrl = "baseFileUrl"
            every { cloudService.baseFileUrl() } returns (baseFileUrl)

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE) as List<TaskResponse>

            // THEN
            foundTasks.size shouldBe 1
            val answers = foundTasks.first().answerOptions
            val wordMak = answers.first { it.word == "мак" }
            val wordMaki = answers.first { it.word == "маки" }
            wordMak.soundsCount shouldBe 1
            wordMaki.soundsCount shouldBe 2
            wordMak.columnNumber shouldBe 0
            wordMaki.columnNumber shouldBe 1
            answers.size shouldBe 2
        }

        @Test
        fun `should return tasks by exerciseId(isAudioFileUrlGenerated`() {
            val template = ""
            val resource = Resource(word = "word", locale = BrnLocale.RU.locale)
            val expectedTaskSize = 2
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
            every { exerciseMock.toDto() } returns exerciseDtoMock
            every {
                task1Mock.toWordsGroupSeriesTaskDto(
                    ExerciseType.WORDS_SEQUENCES,
                    template
                )
            } returns taskWordsGroupResponse1Mock
            every {
                task2Mock.toWordsGroupSeriesTaskDto(
                    ExerciseType.WORDS_SEQUENCES,
                    template
                )
            } returns taskWordsGroupResponse2Mock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.WORDS_SEQUENCES.name

            val baseFileUrl = "baseFileUrl"
            every { cloudService.baseFileUrl() } returns (baseFileUrl)

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            foundTasks.size shouldBe expectedTaskSize
        }

        @Test
        fun `should return tasks by exerciseId(SENTENCE)`() {
            // GIVEN
            val expectedTaskSize = 2
            val template = ""
            val resource = Resource(word = "word", locale = BrnLocale.RU.locale)
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
            every {
                task1Mock.toWordsGroupSeriesTaskDto(
                    ExerciseType.SENTENCE,
                    template
                )
            } returns taskWordsGroupResponse1Mock
            every {
                task2Mock.toWordsGroupSeriesTaskDto(
                    ExerciseType.SENTENCE,
                    template
                )
            } returns taskWordsGroupResponse2Mock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.SENTENCE.name

            val baseFileUrl = "baseFileUrl"
            every { cloudService.baseFileUrl() } returns (baseFileUrl)

            // WHEN
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            foundTasks.size shouldBe expectedTaskSize
        }

        @Test
        fun `should return tasks by exerciseId(PHRASES)`() {
            // GIVEN
            val expectedTaskSize = 2
            val resource = Resource(word = "word", locale = BrnLocale.RU.locale)
            every { taskRepositoryMock.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1Mock,
                task2Mock
            )
            every { exerciseRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(exerciseMock)

            every { task1Mock.toTaskResponse(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto1Mock
            every { task2Mock.toTaskResponse(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto2Mock
            every { task1Mock.answerOptions } returns mutableSetOf(resource)
            every { task2Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.toTaskResponse(ExerciseType.PHRASES) } returns taskDto1Mock
            every { task2Mock.toTaskResponse(ExerciseType.PHRASES) } returns taskDto2Mock
            every { exerciseMock.toDto() } returns exerciseDtoMock
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.PHRASES.name

            every { task1Mock.exercise } returns exerciseMock

            val baseFileUrl = "baseFileUrl"
            every { cloudService.baseFileUrl() } returns (baseFileUrl)

            // WHEN
            var foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            foundTasks.size shouldBe expectedTaskSize

            // WHEN
            foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            foundTasks.size shouldBe expectedTaskSize
        }

        @Test
        fun `should throw an exception when there is no task for this signal exercise type`() {
            // GIVEN
            val resource = Resource(word = "word", locale = BrnLocale.RU.locale)
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

            val baseFileUrl = "baseFileUrl"
            every { cloudService.baseFileUrl() } returns (baseFileUrl)

            // THEN
            shouldThrowExactly<EntityNotFoundException> {
                taskService.getTasksByExerciseId(LONG_ONE)
            }
        }

        @Test
        fun `should return task by id(SINGLE_SIMPLE_WORDS)`() {
            // GIVEN
            val taskDto = TaskResponse(id = 1L, exerciseType = ExerciseType.SINGLE_SIMPLE_WORDS)
            every { taskRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(task1Mock)
            every { task1Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.exercise } returns exerciseMock
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.SINGLE_SIMPLE_WORDS.name
            every { task1Mock.toTaskResponse(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            taskById shouldBe taskDto
            (taskById as TaskResponse).exerciseMechanism shouldBe ExerciseMechanism.WORDS
            taskById.shouldBeWithPictures shouldBe true
        }

        @Test
        fun `should return task by id(WORDS_SEQUENCES)`() {
            // GIVEN
            val template = ""
            val taskDto = TaskWordsGroupResponse(id = 1L, exerciseType = ExerciseType.WORDS_SEQUENCES)
            every { taskRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(task1Mock)
            every { task1Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.exercise } returns exerciseMock
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { task1Mock.id } returns 1L
            every { seriesMock.type } returns ExerciseType.WORDS_SEQUENCES.name
            every { exerciseMock.template } returns template
            every {
                task1Mock.toWordsGroupSeriesTaskDto(
                    ExerciseType.WORDS_SEQUENCES,
                    template
                )
            } returns taskDto

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            assertSame(taskDto, taskById)
            (taskById as TaskWordsGroupResponse).exerciseMechanism shouldBe ExerciseMechanism.MATRIX
            taskById.shouldBeWithPictures shouldBe true
        }

        @Test
        fun `should return task by id(SENTENCE)`() {
            // GIVEN
            val template = ""
            val taskDto = TaskWordsGroupResponse(id = 1L, exerciseType = ExerciseType.SENTENCE)
            every { taskRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(task1Mock)
            every { task1Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.exercise } returns exerciseMock
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { task1Mock.id } returns 1L
            every { seriesMock.type } returns ExerciseType.SENTENCE.name
            every { exerciseMock.template } returns template
            every {
                task1Mock.toWordsGroupSeriesTaskDto(
                    ExerciseType.SENTENCE,
                    template
                )
            } returns taskDto

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            taskById shouldBe taskDto
            (taskById as TaskWordsGroupResponse).exerciseMechanism shouldBe ExerciseMechanism.MATRIX
            taskById.shouldBeWithPictures shouldBe true
        }

        @Test
        fun `should return correct task by id(PHRASES)`() {
            // GIVEN
            val task1 = Task(exercise = exerciseMock, id = LONG_ONE)
            every { taskRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(task1)
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.PHRASES.name

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            (taskById as TaskResponse).exerciseMechanism shouldBe ExerciseMechanism.WORDS
            taskById.id shouldBe LONG_ONE
            taskById.shouldBeWithPictures shouldBe false
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
            every { resourceRepositoryMock.saveAll(any<List<Resource>>()) } returns resources

            // WHEN
            val task = taskService.save(task1Mock)

            // THEN
            task shouldBe task1Mock
        }

        @ParameterizedTest
        @ValueSource(strings = ["мышь", "кот", "смрад"])
        fun `should find Syllable 1 Count`(word: String) {
            Assertions.assertThat(word.findSyllableCount()).isEqualTo(1)
        }

        @ParameterizedTest
        @ValueSource(strings = ["мышка", "кошка", "муан", "портфель"])
        fun `should find Syllable 2 Count`(word: String) {
            Assertions.assertThat(word.findSyllableCount()).isEqualTo(2)
        }

        @ParameterizedTest
        @ValueSource(strings = ["машина", "королёв", "моошка"])
        fun `should find Syllable 3 Count`(word: String) {
            Assertions.assertThat(word.findSyllableCount()).isEqualTo(3)
        }

        @Test
        fun `should calculate columns for Koroleva tasks`() {
            // GIVEN
            val resource1 = Resource(word = "круг", wordType = WordType.OBJECT.name)
            val resource2 = Resource(word = "спать", wordType = WordType.OBJECT.name)
            val resource3 = Resource(word = "мышь", wordType = WordType.OBJECT.name)
            val resource4 = Resource(word = "машина", wordType = WordType.OBJECT.name)
            val resource5 = Resource(word = "рубашка", wordType = WordType.OBJECT.name)
            val resource6 = Resource(word = "голова", wordType = WordType.OBJECT.name)
            val words = mutableSetOf(resource5, resource2, resource1, resource3, resource4, resource6)
            // WHEN
            val result = words.toResourceDtoSet()
            // THEN
            Assertions.assertThat(result.first { it.word == "круг" }.columnNumber).isEqualTo(0)
            Assertions.assertThat(result.first { it.word == "спать" }.columnNumber).isEqualTo(0)
            Assertions.assertThat(result.first { it.word == "мышь" }.columnNumber).isEqualTo(0)
            Assertions.assertThat(result.first { it.word == "машина" }.columnNumber).isEqualTo(1)
            Assertions.assertThat(result.first { it.word == "рубашка" }.columnNumber).isEqualTo(1)
            Assertions.assertThat(result.first { it.word == "голова" }.columnNumber).isEqualTo(1)
        }
    }
}
