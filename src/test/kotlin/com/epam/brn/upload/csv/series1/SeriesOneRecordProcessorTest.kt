package com.epam.brn.upload.csv.series1

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
import java.util.Random
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils

@ExtendWith(MockitoExtension::class)
internal class SeriesOneRecordProcessorTest {

    private val seriesRepositoryMock = mock(SeriesRepository::class.java)
    private val resourceRepositoryMock = mock(ResourceRepository::class.java)
    private val exerciseRepositoryMock = mock(ExerciseRepository::class.java)
    private val resourceCreationService = mock(ResourceCreationService::class.java)

    private lateinit var seriesOneRecordProcessor: SeriesOneRecordProcessor

    private val series = Series(
        id = 1L,
        name = "Распознавание простых слов",
        description = "Распознавание простых слов",
        exerciseGroup = ExerciseGroup(
            id = 2L,
            name = "Речевые упражнения",
            description = "Речевые упражнения"
        )
    )

    private val exerciseName = "Однослоговые слова без шума"
    private val noiseLevel = "no_noise"
    private val words = listOf("(бал", "бум", "быль", "вить", "гад", "дуб)")

    @BeforeEach
    internal fun setUp() {
        seriesOneRecordProcessor = SeriesOneRecordProcessor(
            seriesRepositoryMock,
            resourceRepositoryMock,
            exerciseRepositoryMock,
            resourceCreationService
        )

        ReflectionTestUtils.setField(seriesOneRecordProcessor, "pictureDefaultPath", "pictures/%s.jpg")

        `when`(seriesRepositoryMock.findById(1L)).thenReturn(Optional.of(series))

        mockFindResourceByWordLike("бал", resource_бал())
        mockFindResourceByWordLike("бум", resource_бум())
        mockFindResourceByWordLike("быль", resource_быль())
        mockFindResourceByWordLike("вить", resource_вить())
        mockFindResourceByWordLike("гад", resource_гад())
        mockFindResourceByWordLike("дуб", resource_дуб())
    }

    private fun mockFindResourceByWordLike(word: String, result: Resource) {
        `when`(resourceRepositoryMock.findFirstByWordLike(word)).thenReturn(Optional.of(result))
    }

    @Test
    fun `should create correct exercise`() {
        val expected = createExercise()

        val actual = seriesOneRecordProcessor.process(
            mutableListOf(
                SeriesOneRecord(
                    level = 1,
                    exerciseName = exerciseName,
                    words = words,
                    noise = noiseLevel
                )
            )
        ).first()

        assertThat(actual).isEqualTo(expected)
        verify(exerciseRepositoryMock).save(expected)
    }

    // @Test
    fun `should create correct task`() {
        seriesOneRecordProcessor.random = Random(800)
        val expected = createExercise().tasks.first()

        val actual = seriesOneRecordProcessor.process(
            mutableListOf(
                SeriesOneRecord(
                    level = 1,
                    exerciseName = exerciseName,
                    noise = noiseLevel,
                    words = listOf("(бал", "бум", "быль)")
                )
            )
        ).first().tasks.first()

        assertThat(actual).isEqualToIgnoringGivenFields(expected, "answerOptions")
    }

    @Test
    fun `should create correct answer options`() {
        val expected = setOf(
            resource_бал(), resource_бум(), resource_быль(),
            resource_вить(), resource_гад(), resource_дуб()
        )

        val tasks = seriesOneRecordProcessor
            .process(mutableListOf(SeriesOneRecord(1, exerciseName, words, noiseLevel)))
            .first().tasks

        tasks.forEach {
            assertThat(it.answerOptions).containsExactlyElementsOf(expected)
        }

        verify(resourceRepositoryMock).saveAll(expected)
    }

    private fun createExercise(): Exercise {
        val exercise = Exercise(
            series = series,
            name = exerciseName,
            description = exerciseName,
            exerciseType = ExerciseType.SINGLE_SIMPLE_WORDS.toString(),
            level = 1
        )

        exercise.addTasks(createTasks(exercise))

        return exercise
    }

    private fun createTasks(exercise: Exercise): List<Task> {
        return listOf(
            Task(
                exercise = exercise,
                serialNumber = 1,
                answerOptions = mutableSetOf(resource_бал(), resource_бум(), resource_быль()),
                correctAnswer = resource_бал()
            ), Task(
                exercise = exercise,
                serialNumber = 2,
                answerOptions = mutableSetOf(resource_бал(), resource_бум(), resource_быль()),
                correctAnswer = resource_бум()
            ), Task(
                exercise = exercise,
                serialNumber = 3,
                answerOptions = mutableSetOf(resource_бал(), resource_бум(), resource_быль()),
                correctAnswer = resource_быль()
            ), Task(
                exercise = exercise,
                serialNumber = 4,
                answerOptions = mutableSetOf(resource_бал(), resource_бум(), resource_быль()),
                correctAnswer = resource_бал()
            ), Task(
                exercise = exercise,
                serialNumber = 5,
                answerOptions = mutableSetOf(resource_бал(), resource_бум(), resource_быль()),
                correctAnswer = resource_бум()
            ), Task(
                exercise = exercise,
                serialNumber = 6,
                answerOptions = mutableSetOf(resource_бал(), resource_бум(), resource_быль()),
                correctAnswer = resource_быль()
            )
        )
    }

    private fun resource_бал(): Resource {
        return Resource(
            word = "бал",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "no_noise/бал.mp3",
            pictureFileUrl = "pictures/бал.jpg"
        )
    }

    private fun resource_бум(): Resource {
        return Resource(
            word = "бум",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "no_noise/бум.mp3",
            pictureFileUrl = "pictures/бум.jpg"
        )
    }

    private fun resource_быль(): Resource {
        return Resource(
            word = "быль",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "no_noise/быль.mp3",
            pictureFileUrl = "pictures/быль.jpg"
        )
    }

    private fun resource_вить(): Resource {
        return Resource(
            word = "вить",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "no_noise/вить.mp3",
            pictureFileUrl = "pictures/вить.jpg"
        )
    }

    private fun resource_гад(): Resource {
        return Resource(
            word = "гад",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "no_noise/гад.mp3",
            pictureFileUrl = "pictures/гад.jpg"
        )
    }

    private fun resource_дуб(): Resource {
        return Resource(
            word = "дуб",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "no_noise/дуб.mp3",
            pictureFileUrl = "pictures/дуб.jpg"
        )
    }
}
