package com.epam.brn.upload.csv.seriesMatrix

import com.epam.brn.enums.Locale
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
import com.epam.brn.model.WordType
import com.epam.brn.repo.TaskRepository
import com.epam.brn.service.AudioFileMetaData
import com.epam.brn.service.WordsService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
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
        ReflectionTestUtils.setField(seriesMatrixRecordProcessor, "pictureWithWordFileUrl", "pictures/withWord/%s.jpg")
        every { subGroupRepositoryMock.findByCodeAndLocale("code", Locale.RU.locale) } returns subGroupMock
        every { wordsServiceMock.getDefaultManVoiceForLocale(Locale.RU.locale) } returns Voice.FILIPP
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

        mockFindResourceByWordLike("девочка", resource_девочка())
        mockFindResourceByWordLike("бабушка", resource_бабушка())
        mockFindResourceByWordLike("дедушка", resource_дедушка())
        mockFindResourceByWordLike("сидит", resource_сидит())
        mockFindResourceByWordLike("лежит", resource_лежит())
        mockFindResourceByWordLike("идет", resource_идет())
    }

    private fun mockFindResourceByWordLike(word: String, result: Resource) {
        every { resourceRepositoryMock.findFirstByWordLike(word) } returns Optional.of(result)
    }

    @Test
    fun `should create correct exercise`() {
        // GIVEN
        val expected = createExercise()
        every { exerciseRepositoryMock.save(expected) } returns expected
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
        assertThat(actual).isEqualTo(expected)
        verify { exerciseRepositoryMock.save(expected) }
    }

    @Test
    fun `should create correct task`() {
        // GIVEN
        val exercise = createExercise()
        val expectedTask = exercise.tasks.first()
        every { exerciseRepositoryMock.save(exercise) } returns exercise
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
        ).first().tasks.first()
        // THEN
        assertThat(actual).isEqualToIgnoringGivenFields(expectedTask, "answerOptions")
    }

    @Test
    fun `should create correct task with different caml case in input`() {
        // GIVEN
        val exercise = createExercise()
        val expectedTask = exercise.tasks.first()
        every { exerciseRepositoryMock.save(exercise) } returns exercise
        // WHEN
        val actual = seriesMatrixRecordProcessor.process(
            mutableListOf(
                SeriesMatrixRecord(
                    level = 1,
                    code = "code",
                    exerciseName = "Шесть слов",
                    orderNumber = 1,
                    words = listOf("(()", "()", "(Девочка бабушка дедушкА)", "(сидит леЖит идет)", "()", "())")
                )
            )
        ).first().tasks.first()
        // THEN
        assertThat(actual).isEqualToIgnoringGivenFields(expectedTask, "answerOptions")
    }

    @Test
    fun `should create correct answer options`() {
        // GIVEN
        val exercise = createExercise()
        every { exerciseRepositoryMock.save(exercise) } returns exercise
        val expectedResources = setOf(
            resource_девочка(),
            resource_бабушка(),
            resource_дедушка(),
            resource_сидит(),
            resource_лежит(),
            resource_идет()
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
        // THEN
        assertThat(actual).containsExactlyElementsOf(expectedResources)
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
            answerOptions = mutableSetOf(
                resource_девочка(),
                resource_бабушка(),
                resource_дедушка(),
                resource_сидит(),
                resource_лежит(),
                resource_идет()
            )
        )
    }

    private fun resource_девочка(): Resource {
        return Resource(
            word = "девочка",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "audio/ogg/filipp/девочка.ogg",
            pictureFileUrl = "pictures/withWord/девочка.jpg"
        )
    }

    private fun resource_бабушка(): Resource {
        return Resource(
            word = "бабушка",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "audio/ogg/filipp/бабушка.ogg",
            pictureFileUrl = "pictures/withWord/бабушка.jpg"
        )
    }

    private fun resource_дедушка(): Resource {
        return Resource(
            word = "дедушка",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "audio/ogg/filipp/дедушка.ogg",
            pictureFileUrl = "pictures/withWord/дедушка.jpg"
        )
    }

    private fun resource_сидит(): Resource {
        return Resource(
            word = "сидит",
            wordType = WordType.OBJECT_ACTION.toString(),
            audioFileUrl = "audio/ogg/filipp/сидит.ogg",
            pictureFileUrl = "pictures/withWord/сидит.jpg"
        )
    }

    private fun resource_лежит(): Resource {
        return Resource(
            word = "лежит",
            wordType = WordType.OBJECT_ACTION.toString(),
            audioFileUrl = "audio/ogg/filipp/лежит.ogg",
            pictureFileUrl = "pictures/withWord/лежит.jpg"
        )
    }

    private fun resource_идет(): Resource {
        return Resource(
            word = "идет",
            wordType = WordType.OBJECT_ACTION.toString(),
            audioFileUrl = "audio/ogg/filipp/идет.ogg",
            pictureFileUrl = "pictures/withWord/идет.jpg"
        )
    }
}
