package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.response.WordsGroupSeriesTaskResponse
import com.epam.brn.dto.response.WordsTaskResponse
import com.epam.brn.enums.BrnLocale
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.model.WordType
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
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.util.ReflectionTestUtils
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
    lateinit var urlConversionService: UrlConversionService

    @MockK
    lateinit var task1Mock: Task

    @MockK
    lateinit var task2Mock: Task

    @MockK
    lateinit var resourceMock: Resource

    @MockK
    lateinit var taskDto1Mock: WordsTaskResponse

    @MockK
    lateinit var taskDto2Mock: WordsTaskResponse

    @MockK
    lateinit var wordsGroupSeriesTaskResponse1Mock: WordsGroupSeriesTaskResponse

    @MockK
    lateinit var wordsGroupSeriesTaskResponse2Mock: WordsGroupSeriesTaskResponse

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
            val resource = Resource(word = "word", locale = BrnLocale.RU.locale)
            every { taskRepositoryMock.findTasksByExerciseIdWithJoinedAnswers(ofType(Long::class)) } returns listOf(
                task1Mock,
                task2Mock
            )
            every { exerciseRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(exerciseMock)

            every { task1Mock.answerOptions } returns mutableSetOf(resource)
            every { task2Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.toWordsTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto1Mock
            every { task2Mock.toWordsTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto2Mock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.SINGLE_SIMPLE_WORDS.name

            every { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"
            every { urlConversionService.makeUrlForTaskPicture(resource.word) } returns "fullPictureUrl"

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
            every { task1Mock.toWordsGroupSeriesTaskDto(template) } returns wordsGroupSeriesTaskResponse1Mock
            every { task2Mock.toWordsGroupSeriesTaskDto(template) } returns wordsGroupSeriesTaskResponse2Mock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.WORDS_SEQUENCES.name

            every { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"
            every { urlConversionService.makeUrlForTaskPicture(resource.word) } returns "fullPictureUrl"

            // WHEN isAudioFileUrlGenerated = false
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
            // every { task1Mock.toWordsGroupSeriesTaskDto(template) } returns wordsGroupSeriesTaskResponse1Mock

            every { exerciseMock.template } returns template
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.SINGLE_WORDS_KOROLEVA.name

            every { wordsServiceMock.getFullS3UrlForWord(any(), any()) } returns "fullUrl"
            every { urlConversionService.makeUrlForTaskPicture(any()) } returns "fullPictureUrl"

            // WHEN isAudioFileUrlGenerated = false
            val foundTasks = taskService.getTasksByExerciseId(LONG_ONE) as List<WordsTaskResponse>

            // THEN
            foundTasks.size shouldBe 1
            val answers = foundTasks.first().answerOptions
            val taskMak = answers.first { it.word == "мак" }
            val taskMaki = answers.first { it.word == "маки" }
            taskMak.soundsCount shouldBe 1
            taskMaki.soundsCount shouldBe 2
            taskMak.columnNumber shouldBe 0
            taskMaki.columnNumber shouldBe 1
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
            every { task1Mock.toWordsGroupSeriesTaskDto(template) } returns wordsGroupSeriesTaskResponse1Mock
            every { task2Mock.toWordsGroupSeriesTaskDto(template) } returns wordsGroupSeriesTaskResponse2Mock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.WORDS_SEQUENCES.name
            every { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"
            every { urlConversionService.makeUrlForTaskPicture(resource.word) } returns "fullPictureUrl"

            // WHEN  isAudioFileUrlGenerated = true
            ReflectionTestUtils.setField(taskService, "getAudioFileFromStorage", true)

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
            every { task1Mock.toSentenceSeriesTaskDto(template) } returns wordsGroupSeriesTaskResponse1Mock
            every { task2Mock.toSentenceSeriesTaskDto(template) } returns wordsGroupSeriesTaskResponse2Mock

            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.SENTENCE.name

            every { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"
            every { urlConversionService.makeUrlForTaskPicture(resource.word) } returns "fullPictureUrl"

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

            every { task1Mock.toWordsTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto1Mock
            every { task2Mock.toWordsTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto2Mock
            every { task1Mock.answerOptions } returns mutableSetOf(resource)
            every { task2Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.toPhraseSeriesTaskDto() } returns taskDto1Mock
            every { task2Mock.toPhraseSeriesTaskDto() } returns taskDto2Mock
            every { exerciseMock.toDto() } returns exerciseDtoMock
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.PHRASES.name

            every { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"
            every { urlConversionService.makeUrlForTaskPicture(any()) } returns "fullPictureUrl"
            every { task1Mock.exercise } returns exerciseMock

            // WHEN
            var foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            foundTasks.size shouldBe expectedTaskSize

            // WHEN  isAudioFileUrlGenerated = true
            ReflectionTestUtils.setField(taskService, "getAudioFileFromStorage", true)
            foundTasks = taskService.getTasksByExerciseId(LONG_ONE)

            // THEN
            verify(exactly = 1) { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) }
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

            every { wordsServiceMock.getFullS3UrlForWord(resource.word, resource.locale) } returns "fullUrl"
            every { urlConversionService.makeUrlForTaskPicture(resource.word) } returns "fullPictureUrl"

            // THEN
            shouldThrowExactly<EntityNotFoundException> {
                taskService.getTasksByExerciseId(LONG_ONE)
            }
        }

        @Test
        fun `should return task by id(SINGLE_SIMPLE_WORDS)`() {
            // GIVEN
            val taskDto = WordsTaskResponse(id = 1L, exerciseType = ExerciseType.SINGLE_SIMPLE_WORDS)
            every { taskRepositoryMock.findById(ofType(Long::class)) } returns Optional.of(task1Mock)
            every { task1Mock.answerOptions } returns mutableSetOf()
            every { task1Mock.exercise } returns exerciseMock
            every { exerciseMock.subGroup } returns subGroupMock
            every { subGroupMock.series } returns seriesMock
            every { seriesMock.type } returns ExerciseType.SINGLE_SIMPLE_WORDS.name
            every { task1Mock.toWordsTaskDto(ExerciseType.SINGLE_SIMPLE_WORDS) } returns taskDto

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
            every { task1Mock.toWordsGroupSeriesTaskDto(template) } returns wordsGroupSeriesTaskResponse1Mock

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            assertSame(wordsGroupSeriesTaskResponse1Mock, taskById)
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
            every { task1Mock.toSentenceSeriesTaskDto(template) } returns wordsGroupSeriesTaskResponse1Mock

            // WHEN
            val taskById = taskService.getTaskById(LONG_ONE)

            // THEN
            taskById shouldBe wordsGroupSeriesTaskResponse1Mock
        }

        @Test
        fun `should return task by id(PHRASES)`() {
            // GIVEN
            val taskDto = WordsTaskResponse(id = 1L, exerciseType = ExerciseType.PHRASES)
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
