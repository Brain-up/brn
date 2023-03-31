package com.epam.brn.upload.csv.seriesMatrix

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.Voice
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.repo.TaskRepository
import com.epam.brn.service.WordsService
import com.epam.brn.utils.resource
import io.kotest.matchers.equality.shouldBeEqualToUsingFields
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class SeriesMatrixRecordProcessorTest {

    @MockK
    private lateinit var seriesRepositoryMock: SeriesRepository

    @MockK
    private lateinit var subGroupRepositoryMock: SubGroupRepository

    @MockK
    private lateinit var exerciseRepositoryMock: ExerciseRepository

    @MockK
    private lateinit var resourceRepositoryMock: ResourceRepository

    @MockK
    private lateinit var taskRepositoryMock: TaskRepository

    @MockK(relaxed = true)
    private lateinit var wordsServiceMock: WordsService

    @MockK
    private lateinit var subGroupMock: SubGroup

    @InjectMockKs
    private lateinit var seriesMatrixRecordProcessor: SeriesMatrixRecordProcessor

    private val series = Series(
        id = 2L,
        level = 1,
        type = "type",
        name = "Распознавание последовательности слов",
        description = "Распознавание последовательности слов",
        exerciseGroup = ExerciseGroup(
            code = "SPEECH_RU_RU",
            name = "Речевые упражнения",
            description = "Речевые упражнения"
        )
    )

    private val subGroup = SubGroup(
        series = series,
        level = 1,
        code = "code",
        name = "subGroup name"
    )

    @BeforeEach
    internal fun setUp() {
        seriesMatrixRecordProcessor = SeriesMatrixRecordProcessor(
            subGroupRepositoryMock,
            resourceRepositoryMock,
            exerciseRepositoryMock,
            taskRepositoryMock,
            wordsServiceMock
        )
        every { subGroupRepositoryMock.findByCodeAndLocale("code", BrnLocale.RU.locale) } returns subGroupMock
        every { wordsServiceMock.getDefaultManVoiceForLocale(BrnLocale.RU.locale) } returns Voice.FILIPP.name
        every { exerciseRepositoryMock.findExerciseByNameAndLevel(any(), any()) } returns Optional.empty()
        every {
            resourceRepositoryMock.findFirstByWordAndLocaleAndWordType(
                ofType(String::class),
                ofType(String::class),
                ofType(String::class)
            )
        } returns Optional.empty()
        every { wordsServiceMock.getSubFilePathForWord(ofType(AudioFileMetaData::class)) } returns String()
        every { resourceRepositoryMock.saveAll(any<List<Resource>>()) } returns emptySet()
        every { taskRepositoryMock.save(ofType(Task::class)) } returns Task()
        every { seriesRepositoryMock.findById(2L) } returns Optional.of(series)

        mockFindResourceByWordLike("девочка", createResource("девочка"))
        mockFindResourceByWordLike("бабушка", createResource("бабушка"))
        mockFindResourceByWordLike("дедушка", createResource("дедушка"))
        mockFindResourceByWordLike("сидит", createResource("сидит"))
        mockFindResourceByWordLike("лежит", createResource("лежит"))
        mockFindResourceByWordLike("идет", createResource("идет"))
    }

    private fun mockFindResourceByWordLike(word: String, result: Resource) {
        every { resourceRepositoryMock.findFirstByWordLike(word) } returns Optional.of(result)
    }

    @Test
    fun `should create correct exercise`() {
        // GIVEN
        val expected = createExercise()
        every { exerciseRepositoryMock.save(any()) } returnsArgument 0
        // WHEN
        val actual = seriesMatrixRecordProcessor.process(
            mutableListOf(
                SeriesMatrixRecord(
                    level = 1,
                    code = "code",
                    exerciseName = "Шесть слов",
                    orderNumber = 1,
                    words = listOf("(()", "()", "(девочка бабушка дедушка)", "(сидит лежит идет)", "()", "())")
                )
            )
        ).first()
        // THEN
        actual.shouldBeEqualToUsingFields(expected, Exercise::name, Exercise::level)
        verify { exerciseRepositoryMock.save(match { it.name == expected.name && it.level == expected.level }) }
    }

    @Test
    fun `should create correct task`() {
        // GIVEN
        val exercise = createExercise()
        val expectedTask = exercise.tasks.first()
        every { exerciseRepositoryMock.save(any()) } returnsArgument 0
        // WHEN
        seriesMatrixRecordProcessor.process(
            mutableListOf(
                SeriesMatrixRecord(
                    level = 1,
                    code = "code",
                    exerciseName = "Шесть слов",
                    orderNumber = 1,
                    words = listOf("(()", "()", "(девочка бабушка дедушка)", "(сидит лежит идет)", "()", "())")
                )
            )
        ).first().shouldBeEqualToUsingFields(exercise, Exercise::name, Exercise::level)
        // THEN
        verify {
            taskRepositoryMock.save(
                match {
                    it.name == expectedTask.name &&
                        it.exercise!!.name == exercise.name &&
                        it.exercise!!.level == exercise.level
                }
            )
        }
    }

    @Test
    fun `should create correct answer options`() {
        // GIVEN
        every { exerciseRepositoryMock.save(any()) } returnsArgument 0
        every { taskRepositoryMock.save(any()) } answers {
            val task = it.invocation.args[0] as Task
            val exercise = task.exercise
            exercise?.tasks?.add(task)
            return@answers task
        }
        every { resourceRepositoryMock.saveAll(ofType<ArrayList<Resource>>()) } returnsArgument 0
        every { wordsServiceMock.getSubFilePathForWord(ofType(AudioFileMetaData::class)) } answers {
            val data = it.invocation.args[0] as AudioFileMetaData
            return@answers "/test/${data.text}.ogg"
        }

        val expectedResources = listOf(
            createResource("девочка", ""),
            createResource("бабушка", ""),
            createResource("дедушка", ""),
            createResource("сидит", ""),
            createResource("лежит", ""),
            createResource("идет", "")
        )
        // WHEN
        val actual = seriesMatrixRecordProcessor.process(
            mutableListOf(
                SeriesMatrixRecord(
                    level = 1,
                    code = "code",
                    exerciseName = "Шесть слов",
                    orderNumber = 1,
                    words = listOf("(()", "()", "(девочка бабушка дедушка)", "(сидит лежит идет)", "()", "())")
                )
            )
        ).first().tasks.first().answerOptions

        assertThat(actual).usingElementComparatorOnFields("word", "locale", "audioFileUrl").isEqualTo(expectedResources)
    }

    private fun createExercise(): Exercise {
        val exercise = Exercise(
            subGroup = subGroup,
            name = "Шесть слов",
            template = "<OBJECT OBJECT_ACTION>",
            level = 1
        )
        exercise.addTask(createTask(exercise))
        return exercise
    }

    private fun createTask(exercise: Exercise): Task {
        return Task(
            serialNumber = 2,
            exercise = exercise,
            answerOptions = mutableListOf(
                createResource("девочка"),
                createResource("бабушка"),
                createResource("дедушка"),
                createResource("сидит"),
                createResource("лежит"),
                createResource("идет")
            )
        )
    }

    private fun createResource(word: String, picture: String = "pictures/withWord/$word.jpg"): Resource {
        return resource(word, picture)
    }
}
