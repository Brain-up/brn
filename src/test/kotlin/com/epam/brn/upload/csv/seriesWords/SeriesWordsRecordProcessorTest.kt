package com.epam.brn.upload.csv.seriesWords

import com.epam.brn.enums.Locale
import com.epam.brn.enums.Voice
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.model.WordType
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
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
import java.util.Random

@ExtendWith(MockKExtension::class)
internal class SeriesWordsRecordProcessorTest {

    @MockK
    private lateinit var seriesRepositoryMock: SeriesRepository

    @MockK
    private lateinit var subGroupRepositoryMock: SubGroupRepository

    @MockK
    private lateinit var exerciseRepositoryMock: ExerciseRepository

    @MockK
    private lateinit var resourceRepositoryMock: ResourceRepository

    @MockK
    private lateinit var wordsServiceMock: WordsService

    @MockK
    private lateinit var subGroupMock: SubGroup

    @InjectMockKs
    private lateinit var seriesWordsRecordProcessor: SeriesWordsRecordProcessor

    private val series = Series(
        id = 1L,
        name = "Распознавание простых слов",
        type = "type",
        level = 1,
        description = "Распознавание простых слов",
        exerciseGroup = ExerciseGroup(
            code = "SPEECH_RU_RU",
            name = "Речевые упражнения",
            description = "Речевые упражнения"
        )
    )

    private val exerciseName = "Однослоговые слова без шума"
    private val noiseLevel = 1
    private val noiseUrl = "url"
    private val words = listOf("(бал", "бум", "быль", "вить", "гад", "дуб)")

    @BeforeEach
    internal fun setUp() {
        seriesWordsRecordProcessor = SeriesWordsRecordProcessor(
            subGroupRepositoryMock,
            resourceRepositoryMock,
            exerciseRepositoryMock,
            wordsServiceMock
        )

        ReflectionTestUtils.setField(seriesWordsRecordProcessor, "pictureDefaultPath", "pictures/%s.jpg")
        ReflectionTestUtils.setField(seriesWordsRecordProcessor, "fonAudioPath", "/fon/%s.ogg")
        ReflectionTestUtils.setField(seriesWordsRecordProcessor, "pictureTheme", "/picturesTheme/%s.jpg")

        every { seriesRepositoryMock.findById(1L) } returns Optional.of(series)
        every { subGroupRepositoryMock.findByCodeAndLocale("pictureUrl", Locale.RU.locale) } returns subGroupMock
        every { wordsServiceMock.getSubFilePathForWord(ofType(AudioFileMetaData::class)) } returns String()
        every {
            resourceRepositoryMock.findFirstByWordAndLocaleAndWordType(
                ofType(String::class),
                ofType(String::class),
                ofType(String::class)
            )
        } returns Optional.empty()
        every { wordsServiceMock.addWordsToDictionary(ofType(Locale::class), any()) } returns Unit
        every { wordsServiceMock.getDefaultManVoiceForLocale(Locale.RU.locale) } returns Voice.FILIPP
        every { exerciseRepositoryMock.findExerciseByNameAndLevel(exerciseName, noiseLevel) } returns Optional.empty()
        every { resourceRepositoryMock.saveAll(any()) } returns emptySet()
        every { exerciseRepositoryMock.save(ofType(Exercise::class)) } returns Exercise()

        mockFindResourceByWordLike("бал", resource_бал())
        mockFindResourceByWordLike("бум", resource_бум())
        mockFindResourceByWordLike("быль", resource_быль())
        mockFindResourceByWordLike("вить", resource_вить())
        mockFindResourceByWordLike("гад", resource_гад())
        mockFindResourceByWordLike("дуб", resource_дуб())
    }

    private fun mockFindResourceByWordLike(word: String, result: Resource) {
        every { resourceRepositoryMock.findFirstByWordLike(word) } returns Optional.of(result)
    }

    @Test
    fun `should create correct exercise`() {
        val expected = createExercise()
        val actual = seriesWordsRecordProcessor.process(
            mutableListOf(
                SeriesWordsRecord(
                    level = 1,
                    code = "pictureUrl",
                    exerciseName = exerciseName,
                    words = words,
                    noiseLevel = noiseLevel,
                    noiseUrl = noiseUrl
                )
            )
        ).first()

        assertThat(actual).isEqualTo(expected)
        verify { exerciseRepositoryMock.save(expected) }
    }

    // @Test
    fun `should create correct task`() {
        seriesWordsRecordProcessor.random = Random(800)
        val expected = createExercise().tasks.first()

        val actual = seriesWordsRecordProcessor.process(
            mutableListOf(
                SeriesWordsRecord(
                    level = 1,
                    code = "pictureUrl",
                    exerciseName = exerciseName,
                    noiseLevel = noiseLevel,
                    noiseUrl = noiseUrl,
                    words = listOf("(бал", "бум", "быль)")
                )
            )
        ).first().tasks.first()

        assertThat(actual).isEqualToIgnoringGivenFields(expected, "answerOptions")
    }

    @Test
    fun `should create correct answer options`() {
        val expected = setOf(
            resource_бал(),
            resource_бум(),
            resource_быль(),
            resource_вить(),
            resource_гад(),
            resource_дуб()
        )
        every { subGroupRepositoryMock.findByCodeAndLocale("pictureUrl", Locale.RU.locale) } returns subGroupMock
        every { wordsServiceMock.getDefaultManVoiceForLocale(Locale.RU.locale) } returns Voice.FILIPP
        every { exerciseRepositoryMock.findExerciseByNameAndLevel(exerciseName, noiseLevel) } returns Optional.empty()
        every {
            wordsServiceMock.getSubFilePathForWord(
                AudioFileMetaData(
                    "бал",
                    Locale.RU.locale,
                    Voice.FILIPP
                )
            )
        } returns "/test/бал.ogg"
        every {
            wordsServiceMock.getSubFilePathForWord(
                AudioFileMetaData(
                    "бум",
                    Locale.RU.locale,
                    Voice.FILIPP
                )
            )
        } returns "/test/бум.ogg"
        every {
            wordsServiceMock.getSubFilePathForWord(
                AudioFileMetaData(
                    "быль",
                    Locale.RU.locale,
                    Voice.FILIPP
                )
            )
        } returns "/test/быль.ogg"
        every {
            wordsServiceMock.getSubFilePathForWord(
                AudioFileMetaData(
                    "вить",
                    Locale.RU.locale,
                    Voice.FILIPP
                )
            )
        } returns "/test/вить.ogg"
        every {
            wordsServiceMock.getSubFilePathForWord(
                AudioFileMetaData(
                    "гад",
                    Locale.RU.locale,
                    Voice.FILIPP
                )
            )
        } returns "/test/гад.ogg"
        every {
            wordsServiceMock.getSubFilePathForWord(
                AudioFileMetaData(
                    "дуб",
                    Locale.RU.locale,
                    Voice.FILIPP
                )
            )
        } returns "/test/дуб.ogg"

        val tasks = seriesWordsRecordProcessor
            .process(mutableListOf(SeriesWordsRecord(1, "pictureUrl", exerciseName, words, noiseLevel, noiseUrl)))
            .first().tasks

        tasks.forEach {
            assertThat(it.answerOptions).containsExactlyElementsOf(expected)
        }
        verify { resourceRepositoryMock.saveAll(expected) }
    }

    private fun createExercise(): Exercise {
        val exercise = Exercise(
            name = exerciseName,
            noiseLevel = 1,
            noiseUrl = "/fon/url.ogg",
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
            ),
            Task(
                exercise = exercise,
                serialNumber = 2,
                answerOptions = mutableSetOf(resource_бал(), resource_бум(), resource_быль()),
                correctAnswer = resource_бум()
            ),
            Task(
                exercise = exercise,
                serialNumber = 3,
                answerOptions = mutableSetOf(resource_бал(), resource_бум(), resource_быль()),
                correctAnswer = resource_быль()
            ),
            Task(
                exercise = exercise,
                serialNumber = 4,
                answerOptions = mutableSetOf(resource_бал(), resource_бум(), resource_быль()),
                correctAnswer = resource_бал()
            ),
            Task(
                exercise = exercise,
                serialNumber = 5,
                answerOptions = mutableSetOf(resource_бал(), resource_бум(), resource_быль()),
                correctAnswer = resource_бум()
            ),
            Task(
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
            audioFileUrl = "/test/бал.ogg",
            pictureFileUrl = "pictures/бал.jpg"
        )
    }

    private fun resource_бум(): Resource {
        return Resource(
            word = "бум",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/test/бум.ogg",
            pictureFileUrl = "pictures/бум.jpg"
        )
    }

    private fun resource_быль(): Resource {
        return Resource(
            word = "быль",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/test/быль.ogg",
            pictureFileUrl = "pictures/быль.jpg"
        )
    }

    private fun resource_вить(): Resource {
        return Resource(
            word = "вить",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/test/вить.ogg",
            pictureFileUrl = "pictures/вить.jpg"
        )
    }

    private fun resource_гад(): Resource {
        return Resource(
            word = "гад",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/test/гад.ogg",
            pictureFileUrl = "pictures/гад.jpg"
        )
    }

    private fun resource_дуб(): Resource {
        return Resource(
            word = "дуб",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/test/дуб.ogg",
            pictureFileUrl = "pictures/дуб.jpg"
        )
    }
}
