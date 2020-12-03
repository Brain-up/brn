package com.epam.brn.upload.csv.series2

import com.epam.brn.integration.repo.ExerciseRepository
import com.epam.brn.integration.repo.ResourceRepository
import com.epam.brn.integration.repo.SeriesRepository
import com.epam.brn.integration.repo.SubGroupRepository
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.model.WordType
import com.epam.brn.service.WordsService
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional

@ExtendWith(MockitoExtension::class)
internal class SeriesTwoRecordProcessorTest {

    private val seriesRepositoryMock = mock(SeriesRepository::class.java)
    private val subGroupRepositoryMock = mock(SubGroupRepository::class.java)
    private val exerciseRepositoryMock = mock(ExerciseRepository::class.java)
    private val resourceRepositoryMock = mock(ResourceRepository::class.java)
    private val resourceCreationServiceMock = mock(WordsService::class.java)

    private lateinit var seriesTwoRecordProcessor: SeriesTwoRecordProcessor

    private val series = Series(
        id = 2L,
        level = 1,
        type = "type",
        name = "Распознавание последовательности слов",
        description = "Распознавание последовательности слов",
        exerciseGroup = ExerciseGroup(
            id = 2L,
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
        seriesTwoRecordProcessor = SeriesTwoRecordProcessor(
            subGroupRepositoryMock,
            resourceRepositoryMock,
            exerciseRepositoryMock,
            resourceCreationServiceMock
        )

        ReflectionTestUtils.setField(seriesTwoRecordProcessor, "audioPath", "audio/ogg/filipp/%s.ogg")
        ReflectionTestUtils.setField(seriesTwoRecordProcessor, "pictureWithWordFileUrl", "pictures/withWord/%s.jpg")
        ReflectionTestUtils.setField(seriesTwoRecordProcessor, "series2WordsFileName", "words_series2.txt")

        `when`(seriesRepositoryMock.findById(2L)).thenReturn(Optional.of(series))

        mockFindResourceByWordLike("девочка", resource_девочка())
        mockFindResourceByWordLike("бабушка", resource_бабушка())
        mockFindResourceByWordLike("дедушка", resource_дедушка())
        mockFindResourceByWordLike("сидит", resource_сидит())
        mockFindResourceByWordLike("лежит", resource_лежит())
        mockFindResourceByWordLike("идет", resource_идет())
    }

    private fun mockFindResourceByWordLike(word: String, result: Resource) {
        `when`(resourceRepositoryMock.findFirstByWordLike(word)).thenReturn(Optional.of(result))
    }

    @Test
    fun `should create correct exercise`() {
        val expected = createExercise()

        val actual = seriesTwoRecordProcessor.process(
            mutableListOf(
                SeriesTwoRecord(
                    level = 1,
                    code = "code",
                    exerciseName = "Шесть слов",
                    orderNumber = 1,
                    words = listOf("(()", "()", "(девочка бабушка дедушка)", "(сидит лежит идет)", "()", "())")
                )
            )
        ).first()

        assertThat(actual).isEqualTo(expected)
        verify(exerciseRepositoryMock).save(expected)
    }

    @Test
    fun `should create correct task`() {
        val expected = createExercise().tasks.first()

        val actual = seriesTwoRecordProcessor.process(
            mutableListOf(
                SeriesTwoRecord(
                    level = 1,
                    code = "code",
                    exerciseName = "Шесть слов",
                    orderNumber = 1,
                    words = listOf("(()", "()", "(девочка бабушка дедушка)", "(сидит лежит идет)", "()", "())")
                )
            )
        ).first().tasks.first()

        assertThat(actual).isEqualToIgnoringGivenFields(expected, "answerOptions")
    }

    @Test
    fun `should create correct answer options`() {
        val expected = setOf(
            resource_девочка(), resource_бабушка(), resource_дедушка(),
            resource_сидит(), resource_лежит(), resource_идет()
        )

        val actual = seriesTwoRecordProcessor.process(
            mutableListOf(
                SeriesTwoRecord(
                    level = 1,
                    code = "code",
                    exerciseName = "Шесть слов",
                    orderNumber = 1,
                    words = listOf("(()", "()", "(девочка бабушка дедушка)", "(сидит лежит идет)", "()", "())")
                )
            )
        ).first().tasks.first().answerOptions

        assertThat(actual).containsExactlyElementsOf(expected)
        verify(resourceRepositoryMock).saveAll(expected)
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
