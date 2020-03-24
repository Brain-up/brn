package com.epam.brn.upload.csv.processor

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.upload.csv.record.SeriesThreeRecord
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
internal class SeriesThreeRecordProcessorTest {

    private val resourceRepositoryMock = mock(ResourceRepository::class.java)
    private val exerciseRepositoryMock = mock(ExerciseRepository::class.java)
    private val seriesRepositoryMock = mock(SeriesRepository::class.java)
    private val taskRepositoryMock = mock(TaskRepository::class.java)

    private lateinit var test: SeriesThreeRecordProcessor

    private val series = Series(
        id = 3L,
        name = "Распознавание предложений",
        description = "Распознавание предложений",
        exerciseGroup = ExerciseGroup(
            id = 2L,
            name = "Речевые упражнения",
            description = "Речевые упражнения"
        )
    )

    @BeforeEach
    internal fun setUp() {
        test = SeriesThreeRecordProcessor(
            resourceRepositoryMock,
            exerciseRepositoryMock,
            seriesRepositoryMock,
            taskRepositoryMock
        )

        ReflectionTestUtils.setField(test, "audioFileUrl", "series2/%s.mp3")
        ReflectionTestUtils.setField(test, "pictureFileUrl", "pictures/withWord/%s.jpg")

        `when`(seriesRepositoryMock.findById(3L)).thenReturn(Optional.of(this.series))

        mockFindResourceByWordLike("девочка рисует", resource_девочка_бросает())
        mockFindResourceByWordLike("девочка", resource_девочка())
        mockFindResourceByWordLike("дедушка", resource_дедушка())
        mockFindResourceByWordLike("бабушка", resource_бабушка())
        mockFindResourceByWordLike("бросает", resource_бросает())
        mockFindResourceByWordLike("читает", resource_читает())
        mockFindResourceByWordLike("рисует", resource_рисует())
    }

    private fun mockFindResourceByWordLike(word: String, result: Resource) {
        `when`(resourceRepositoryMock.findFirstByWordLike(word)).thenReturn(Optional.of(result))
    }

    @Test
    fun `should create correct exercise`() {
        val expected = createExercise()
        val actual = test.process(
            listOf(
                SeriesThreeRecord(
                    level = 1,
                    exerciseName = "Распознавание предложений из 2 слов",
                    orderNumber = 1,
                    words = mutableListOf(
                        "(()",
                        "()",
                        "(девочка дедушка бабушка)",
                        "(бросает читает рисует)",
                        "()",
                        "())"
                    ),
                    answerAudioFile = "series3/девочка_бросает.mp3",
                    answerParts = "(девочка бросает)"
                )
            )
        ).first()

        assertThat(actual).isEqualTo(expected)
        verify(exerciseRepositoryMock).save(expected)
    }

    @Test
    fun `should create correct task`() {
        val expected = createExercise().tasks.first()
        val actual = test.process(
            listOf(
                SeriesThreeRecord(
                    level = 1,
                    exerciseName = "Распознавание предложений из 2 слов",
                    orderNumber = 1,
                    words = mutableListOf(
                        "(()",
                        "()",
                        "(девочка дедушка бабушка)",
                        "(бросает читает рисует)",
                        "()",
                        "())"
                    ),
                    answerAudioFile = "series3/девочка_бросает.mp3",
                    answerParts = "(девочка бросает)"
                )
            )
        ).first().tasks.first()

        assertThat(actual).isEqualToIgnoringGivenFields(expected, "answerOptions")
        verify(taskRepositoryMock).save(expected)
    }

    @Test
    fun `should create correct answer`() {
        val expected = resource_девочка_бросает()
        val actual = test.process(
            listOf(
                SeriesThreeRecord(
                    level = 1,
                    exerciseName = "Распознавание предложений из 2 слов",
                    orderNumber = 1,
                    words = mutableListOf(
                        "(()",
                        "()",
                        "(девочка дедушка бабушка)",
                        "(бросает читает рисует)",
                        "()",
                        "())"
                    ),
                    answerAudioFile = "series3/девочка_бросает.mp3",
                    answerParts = "(девочка бросает)"
                )
            )
        ).first().tasks.first().correctAnswer

        assertThat(actual).isEqualTo(expected)
        verify(resourceRepositoryMock).save(expected)
    }

    @Test
    fun `should create correct answer options`() {
        val expected = setOf(
            resource_девочка(), resource_дедушка(), resource_бабушка(),
            resource_бросает(), resource_читает(), resource_рисует()
        )
        val actual = test.process(
            listOf(
                SeriesThreeRecord(
                    level = 1,
                    exerciseName = "Распознавание предложений из 2 слов",
                    orderNumber = 1,
                    words = mutableListOf(
                        "(()",
                        "()",
                        "(девочка дедушка бабушка)",
                        "(бросает читает рисует)",
                        "()",
                        "())"
                    ),
                    answerAudioFile = "series3/девочка_бросает.mp3",
                    answerParts = "(девочка бросает)"
                )
            )
        ).first().tasks.first().answerOptions

        assertThat(actual).containsExactlyElementsOf(expected)
        verify(resourceRepositoryMock).saveAll(expected)
    }

    private fun createExercise(): Exercise {
        val exercise = Exercise(
            series = series,
            name = "Распознавание предложений из 2 слов",
            description = "Распознавание предложений из 2 слов",
            template = "<OBJECT OBJECT_ACTION>",
            exerciseType = ExerciseTypeEnum.SENTENCE.toString(),
            level = 1
        )

        val task = createTask()
        task.exercise = exercise

        exercise.addTask(task)
        return exercise
    }

    private fun createTask(): Task {
        val resource1 = resource_девочка()
        val resource6 = resource_бросает()

        return Task(
            serialNumber = 2,
            answerOptions = mutableSetOf(
                resource1,
                resource_дедушка(),
                resource_бабушка(),
                resource_бросает(),
                resource_читает(),
                resource6
            ),
            correctAnswer = resource_девочка_бросает(),
            answerParts = mutableMapOf(1 to resource1, 2 to resource6)
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

    private fun resource_дедушка(): Resource {
        return Resource(
            word = "дедушка",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/дедушка.mp3",
            pictureFileUrl = "pictures/withWord/дедушка.jpg"
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

    private fun resource_бросает(): Resource {
        return Resource(
            word = "бросает",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/бросает.mp3",
            pictureFileUrl = "pictures/withWord/бросает.jpg"
        )
    }

    private fun resource_читает(): Resource {
        return Resource(
            word = "читает",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/читает.mp3",
            pictureFileUrl = "pictures/withWord/читает.jpg"
        )
    }

    private fun resource_рисует(): Resource {
        return Resource(
            word = "рисует",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/рисует.mp3",
            pictureFileUrl = "pictures/withWord/рисует.jpg"
        )
    }

    private fun resource_девочка_бросает(): Resource {
        return Resource(
            word = "девочка бросает",
            wordType = WordTypeEnum.SENTENCE.toString(),
            audioFileUrl = "series3/девочка_бросает.mp3"
        )
    }
}
