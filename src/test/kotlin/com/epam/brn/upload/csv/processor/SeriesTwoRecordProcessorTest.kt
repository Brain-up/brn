package com.epam.brn.upload.csv.processor

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.SeriesService
import com.nhaarman.mockito_kotlin.verify
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

    private val resourceRepositoryMock = mock(ResourceService::class.java)
    private val exerciseRepositoryMock = mock(ExerciseRepository::class.java)
    private val exerciseServiceMock = mock(ExerciseService::class.java)
    private val seriesRepositoryMock = mock(SeriesService::class.java)

    private lateinit var test: SeriesTwoRecordProcessor

    private val series = Series(
        id = 2L,
        name = "Распознование последовательности слов",
        description = "Распознование последовательности слов",
        exerciseGroup = ExerciseGroup(
            id = 2L,
            name = "Речевые упражнения",
            description = "Речевые упражнения"
        )
    )

    @BeforeEach
    internal fun setUp() {
        test = SeriesTwoRecordProcessor(
            resourceRepositoryMock,
            seriesRepositoryMock,
            exerciseServiceMock,
            exerciseRepositoryMock
        )

        ReflectionTestUtils.setField(test, "audioFileUrl", "series2/%s.mp3")
        ReflectionTestUtils.setField(test, "pictureFileUrl", "pictures/withWord/%s.jpg")

        `when`(seriesRepositoryMock.findSeriesForId(2L)).thenReturn(series)
        `when`(exerciseServiceMock.findExerciseByNameAndLevel("Шесть слов", 1))
            .thenThrow(EntityNotFoundException::class.java)

        mockFindResourceByWordLike("девочка", resource_девочка())
        mockFindResourceByWordLike("бабушка", resource_бабушка())
        mockFindResourceByWordLike("дедушка", resource_дедушка())
        mockFindResourceByWordLike("сидит", resource_сидит())
        mockFindResourceByWordLike("лежит", resource_лежит())
        mockFindResourceByWordLike("идет", resource_идет())
    }

    private fun mockFindResourceByWordLike(word: String, result: Resource) {
        `when`(resourceRepositoryMock.findFirstResourceByWordLike(word)).thenReturn(result)
    }

    @Test
    fun `should create correct exercise`() {
        val expected = createExercise()

        val actual = test.process(
            mutableListOf(
                mutableMapOf(
                    "level" to 1,
                    "exerciseName" to "Шесть слов",
                    "orderNumber" to 1,
                    "words" to "(();();(девочка бабушка дедушка);(сидит лежит идет);();())"
                )
            )
        ).first()

        assertThat(actual).isEqualTo(expected)
        verify(exerciseRepositoryMock).saveAll(listOf(expected))
    }

    @Test
    fun `should create correct task`() {
        val expected = createExercise().tasks.first()

        val actual = test.process(
            mutableListOf(
                mutableMapOf(
                    "level" to 1,
                    "exerciseName" to "Шесть слов",
                    "orderNumber" to 1,
                    "words" to "(();();(девочка бабушка дедушка);(сидит лежит идет);();())"
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

        val actual = test.process(
            mutableListOf(
                mutableMapOf(
                    "level" to 1,
                    "exerciseName" to "Шесть слов",
                    "orderNumber" to 1,
                    "words" to "(();();(девочка бабушка дедушка);(сидит лежит идет);();())"
                )
            )
        ).first().tasks.first().answerOptions

        assertThat(actual).containsOnlyElementsOf(expected)
        expected.forEach { verify(resourceRepositoryMock).save(it) }
    }

    private fun createExercise(): Exercise {
        val exercise = Exercise(
            series = series,
            name = "Шесть слов",
            description = "Шесть слов",
            template = "<OBJECT OBJECT_ACTION>",
            exerciseType = ExerciseTypeEnum.WORDS_SEQUENCES.toString(),
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
                resource_сидит()
            )
        )
    }

    private fun resource_девочка(): Resource {
        return Resource(
            word = "девочка",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/девочка.mp3",
            pictureFileUrl = "pictures/withWord/девочка.jpg"
        )
    }

    private fun resource_бабушка(): Resource {
        return Resource(
            word = "бабушка",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/бабушка.mp3",
            pictureFileUrl = "pictures/withWord/бабушка.jpg"
        )
    }

    private fun resource_дедушка(): Resource {
        return Resource(
            word = "дедушка",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/дедушка.mp3",
            pictureFileUrl = "pictures/withWord/дедушка.jpg"
        )
    }

    private fun resource_сидит(): Resource {
        return Resource(
            word = "сидит",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/сидит.mp3",
            pictureFileUrl = "pictures/withWord/сидит.jpg"
        )
    }

    private fun resource_лежит(): Resource {
        return Resource(
            word = "лежит",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/лежит.mp3",
            pictureFileUrl = "pictures/withWord/лежит.jpg"
        )
    }

    private fun resource_идет(): Resource {
        return Resource(
            word = "идет",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/идет.mp3",
            pictureFileUrl = "pictures/withWord/идет.jpg"
        )
    }
}
