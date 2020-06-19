package com.epam.brn.upload.csv.series2

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.model.WordType
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.service.ResourceCreationService
import com.nhaarman.mockito_kotlin.verify
import java.util.Optional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils

@ExtendWith(MockitoExtension::class)
internal class SeriesTwoRecordProcessorTest {

    private val seriesRepositoryMock = mock(SeriesRepository::class.java)
    private val resourceRepositoryMock = mock(ResourceRepository::class.java)
    private val exerciseRepositoryMock = mock(ExerciseRepository::class.java)
    private val resourceCreationServiceMock = mock(ResourceCreationService::class.java)

    private lateinit var seriesTwoRecordProcessor: SeriesTwoRecordProcessor

    private val series = Series(
        id = 2L,
        name = "Распознавание последовательности слов",
        description = "Распознавание последовательности слов",
        exerciseGroup = ExerciseGroup(
            id = 2L,
            name = "Речевые упражнения",
            description = "Речевые упражнения"
        )
    )

    @BeforeEach
    internal fun setUp() {
        seriesTwoRecordProcessor = SeriesTwoRecordProcessor(
            seriesRepositoryMock,
            resourceRepositoryMock,
            exerciseRepositoryMock,
            resourceCreationServiceMock
        )

        ReflectionTestUtils.setField(seriesTwoRecordProcessor, "audioFileUrl", "series2/%s.mp3")
        ReflectionTestUtils.setField(seriesTwoRecordProcessor, "pictureWithWordFileUrl", "pictures/withWord/%s.jpg")

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
            series = series,
            name = "Шесть слов",
            description = "Шесть слов",
            template = "<OBJECT OBJECT_ACTION>",
            exerciseType = ExerciseType.WORDS_SEQUENCES.toString(),
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
            audioFileUrl = "series2/девочка.mp3",
            pictureFileUrl = "pictures/withWord/девочка.jpg"
        )
    }

    private fun resource_бабушка(): Resource {
        return Resource(
            word = "бабушка",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "series2/бабушка.mp3",
            pictureFileUrl = "pictures/withWord/бабушка.jpg"
        )
    }

    private fun resource_дедушка(): Resource {
        return Resource(
            word = "дедушка",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "series2/дедушка.mp3",
            pictureFileUrl = "pictures/withWord/дедушка.jpg"
        )
    }

    private fun resource_сидит(): Resource {
        return Resource(
            word = "сидит",
            wordType = WordType.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/сидит.mp3",
            pictureFileUrl = "pictures/withWord/сидит.jpg"
        )
    }

    private fun resource_лежит(): Resource {
        return Resource(
            word = "лежит",
            wordType = WordType.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/лежит.mp3",
            pictureFileUrl = "pictures/withWord/лежит.jpg"
        )
    }

    private fun resource_идет(): Resource {
        return Resource(
            word = "идет",
            wordType = WordType.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/идет.mp3",
            pictureFileUrl = "pictures/withWord/идет.jpg"
        )
    }
}
