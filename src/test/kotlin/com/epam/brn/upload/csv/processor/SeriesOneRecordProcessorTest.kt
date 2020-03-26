package com.epam.brn.upload.csv.processor

import com.epam.brn.constant.ExerciseType
import com.epam.brn.constant.WordType
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.repo.TaskRepository
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.SeriesService
import com.epam.brn.upload.csv.record.SeriesOneRecord
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils

@ExtendWith(MockitoExtension::class)
internal class SeriesOneRecordProcessorTest {

    private val seriesRepositoryMock = Mockito.mock(SeriesService::class.java)
    private val resourceRepositoryMock = Mockito.mock(ResourceService::class.java)
    private val exerciseRepositoryMock = Mockito.mock(ExerciseService::class.java)
    private val taskRepositoryMock = Mockito.mock(TaskRepository::class.java)

    private lateinit var test: SeriesOneRecordProcessor

    private val series = Series(
        id = 1L,
        name = "Распознование слов",
        description = "Распознование слов",
        exerciseGroup = ExerciseGroup(
            id = 2L,
            name = "Речевые упражнения",
            description = "Речевые упражнения"
        )
    )

    @BeforeEach
    internal fun setUp() {
        test = SeriesOneRecordProcessor(
            seriesRepositoryMock,
            resourceRepositoryMock,
            exerciseRepositoryMock,
            taskRepositoryMock
        )

        ReflectionTestUtils.setField(test, "defaultAudioFileUrl", "default/%s.mp3")

        `when`(seriesRepositoryMock.findSeriesForId(1L)).thenReturn(series)
        `when`(exerciseRepositoryMock.findExerciseByNameAndLevel(createExercise().name, createExercise().level!!))
            .thenReturn(createExercise())

        mockFindResourceByWordLike("бал", resource_бал())
        mockFindResourceByWordLike("бам", resource_бам())
        mockFindResourceByWordLike("сам", resource_сам())
        mockFindResourceByWordLike("дам", resource_дам())
        mockFindResourceByWordLike("зал", resource_зал())
        mockFindResourceByWordLike("бум", resource_бум())
    }

    private fun mockFindResourceByWordLike(word: String, result: Resource) {
        `when`(resourceRepositoryMock.findFirstResourceByWordLike(word)).thenReturn(result)
    }

    @Test
    fun `should create correct exercise`() {
        val expected = createExercise()

        val actual = test.process(
            listOf(
                SeriesOneRecord(
                    level = 1,
                    exerciseName = "Однослоговые слова без шума",
                    orderNumber = 1,
                    word = "бал",
                    audioFileName = "no_noise/бал.mp3",
                    pictureFileName = "pictures/бал.jpg",
                    words = listOf("(бам", "сам", "дам", "зал", "бум"),
                    wordType = "OBJECT"
                )
            )
        ).first()

        Assertions.assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `should create correct task`() {
        val expected = createExercise().tasks.first()
        val actual = test.process(
            listOf(
                SeriesOneRecord(
                    level = 1,
                    exerciseName = "Однослоговые слова без шума",
                    orderNumber = 1,
                    word = "бал",
                    audioFileName = "no_noise/бал.mp3",
                    pictureFileName = "pictures/бал.jpg",
                    words = listOf("(бам", "сам", "дам", "зал", "бум"),
                    wordType = "OBJECT"
                )
            )
        ).first().tasks.first()

        Assertions.assertThat(actual).isEqualToIgnoringGivenFields(expected, "answerOptions")
    }

    @Test
    fun `should create correct answer`() {
        val expected = resource_бал()
        val actual = test.process(
            listOf(
                SeriesOneRecord(
                    level = 1,
                    exerciseName = "Однослоговые слова без шума",
                    orderNumber = 1,
                    word = "бал",
                    audioFileName = "no_noise/бал.mp3",
                    pictureFileName = "pictures/бал.jpg",
                    words = listOf("(бам", "сам", "дам", "зал", "бум"),
                    wordType = "OBJECT"
                )
            )
        ).first().tasks.first().correctAnswer

        Assertions.assertThat(actual).isEqualTo(expected)
        verify(resourceRepositoryMock).save(expected)
    }

    @Test
    fun `should create correct answer options`() {
        val expected = setOf(
            resource_бам(), resource_сам(), resource_дам(), resource_зал(), resource_бум()
        )

        val actual = test.process(
            listOf(
                SeriesOneRecord(
                    level = 1,
                    exerciseName = "Однослоговые слова без шума",
                    orderNumber = 1,
                    word = "бал",
                    audioFileName = "no_noise/бал.mp3",
                    pictureFileName = "pictures/бал.jpg",
                    words = listOf("(бам", "сам", "дам", "зал", "бум"),
                    wordType = "OBJECT"
                )
            )
        ).first().tasks.first().answerOptions

        Assertions.assertThat(actual).containsExactlyElementsOf(expected)
    }

    private fun createExercise(): Exercise {
        val exercise = Exercise(
            series = series,
            name = "Однослоговые слова без шума",
            exerciseType = ExerciseType.SINGLE_WORDS.toString(),
            level = 1
        )

        val task = createTask(exercise)

        exercise.addTask(task)
        return exercise
    }

    private fun createTask(exercise: Exercise): Task {
        return Task(
            serialNumber = 2,
            exercise = exercise,
            answerOptions = mutableSetOf(
                resource_бам(),
                resource_сам(),
                resource_дам(),
                resource_зал(),
                resource_бум()
            ),
            correctAnswer = resource_бал()
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

    private fun resource_бам(): Resource {
        return Resource(
            word = "бам",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "no_noise/бам.mp3",
            pictureFileUrl = "pictures/бам.jpg"
        )
    }

    private fun resource_сам(): Resource {
        return Resource(
            word = "сам",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "no_noise/сам.mp3",
            pictureFileUrl = "pictures/сам.jpg"
        )
    }

    private fun resource_дам(): Resource {
        return Resource(
            word = "дам",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "no_noise/дам.mp3",
            pictureFileUrl = "pictures/дам.jpg"
        )
    }

    private fun resource_зал(): Resource {
        return Resource(
            word = "зал",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "no_noise/зал.mp3",
            pictureFileUrl = "pictures/зал.jpg"
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
}
