package com.epam.brn.upload.csv.seriesWordsKoroleva

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.Voice
import com.epam.brn.enums.WordType
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.service.WordsService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class SeriesWordsKorolevaRecordProcessorTest {

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
    private lateinit var seriesWordsKorolevaRecordProcessor: SeriesWordsKorolevaRecordProcessor

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

    private val level = 1
    private val exerciseName = "Однослоговые слова без шума"
    private val playWordsCount = 1
    private val wordsColumns = 3
    private val words = listOf("(бал", "бум", "быль", "вить", "гад", "дуб)")

    @BeforeEach
    internal fun setUp() {
        seriesWordsKorolevaRecordProcessor = SeriesWordsKorolevaRecordProcessor(
            subGroupRepositoryMock,
            resourceRepositoryMock,
            exerciseRepositoryMock,
            wordsServiceMock
        )

        every { seriesRepositoryMock.findById(1L) } returns Optional.of(series)
        every { subGroupRepositoryMock.findByCodeAndLocale("pictureUrl", BrnLocale.RU.locale) } returns subGroupMock
        every { wordsServiceMock.getSubFilePathForWord(ofType(AudioFileMetaData::class)) } returns String()
        every {
            resourceRepositoryMock.findFirstByWordAndLocaleAndWordType(
                ofType(String::class),
                ofType(String::class),
                ofType(String::class)
            )
        } returns Optional.empty()
        every { wordsServiceMock.addWordsToDictionary(ofType(BrnLocale::class), any()) } returns Unit
        every { wordsServiceMock.getDefaultWomanVoiceForLocale(BrnLocale.RU.locale) } returns Voice.ALYSS.name
        every { exerciseRepositoryMock.findExerciseByNameAndLevel(exerciseName, level) } returns Optional.empty()
        every { resourceRepositoryMock.saveAll(any<List<Resource>>()) } returns emptySet()
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
        val actual = seriesWordsKorolevaRecordProcessor.process(
            mutableListOf(
                SeriesWordsKorolevaRecord(
                    level = level,
                    code = "pictureUrl",
                    exerciseName = exerciseName,
                    words = words,
                    playWordsCount = playWordsCount,
                    wordsColumns = wordsColumns
                )
            )
        ).first()

        assertThat(actual).isEqualTo(expected)
        verify { exerciseRepositoryMock.save(expected) }
    }

    // @Test
    fun `should create correct task`() {
        val expected = createExercise().tasks.first()

        val actual = seriesWordsKorolevaRecordProcessor.process(
            mutableListOf(
                SeriesWordsKorolevaRecord(
                    level = 1,
                    code = "pictureUrl",
                    exerciseName = exerciseName,
                    playWordsCount = 1,
                    wordsColumns = 3,
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
        every { subGroupRepositoryMock.findByCodeAndLocale("pictureUrl", BrnLocale.RU.locale) } returns subGroupMock
        every { wordsServiceMock.getDefaultManVoiceForLocale(BrnLocale.RU.locale) } returns Voice.ALYSS.name
        every { exerciseRepositoryMock.findExerciseByNameAndLevel(exerciseName, 1) } returns Optional.empty()
        every {
            wordsServiceMock.getSubFilePathForWord(
                AudioFileMetaData(
                    "бал",
                    BrnLocale.RU.locale,
                    Voice.ALYSS.name
                )
            )
        } returns "/test/бал.ogg"
        every {
            wordsServiceMock.getSubFilePathForWord(
                AudioFileMetaData(
                    "бум",
                    BrnLocale.RU.locale,
                    Voice.ALYSS.name
                )
            )
        } returns "/test/бум.ogg"
        every {
            wordsServiceMock.getSubFilePathForWord(
                AudioFileMetaData(
                    "быль",
                    BrnLocale.RU.locale,
                    Voice.ALYSS.name
                )
            )
        } returns "/test/быль.ogg"
        every {
            wordsServiceMock.getSubFilePathForWord(
                AudioFileMetaData(
                    "вить",
                    BrnLocale.RU.locale,
                    Voice.ALYSS.name
                )
            )
        } returns "/test/вить.ogg"
        every {
            wordsServiceMock.getSubFilePathForWord(
                AudioFileMetaData(
                    "гад",
                    BrnLocale.RU.locale,
                    Voice.ALYSS.name
                )
            )
        } returns "/test/гад.ogg"
        every {
            wordsServiceMock.getSubFilePathForWord(
                AudioFileMetaData(
                    "дуб",
                    BrnLocale.RU.locale,
                    Voice.ALYSS.name
                )
            )
        } returns "/test/дуб.ogg"

        val tasks = seriesWordsKorolevaRecordProcessor
            .process(
                mutableListOf(
                    SeriesWordsKorolevaRecord(
                        1,
                        "pictureUrl",
                        exerciseName,
                        words,
                        playWordsCount,
                        wordsColumns
                    )
                )
            )
            .first().tasks

        tasks.forEach {
            assertThat(it.answerOptions).containsExactlyElementsOf(expected)
        }
        verify { resourceRepositoryMock.saveAll(expected) }
    }

    @Test
    fun `should throw EntityNotFoundException`() {
        every { subGroupRepositoryMock.findByCodeAndLocale("pictureUrl", BrnLocale.RU.locale) } returns null
        assertThrows(EntityNotFoundException::class.java) {
            seriesWordsKorolevaRecordProcessor.process(
                mutableListOf(
                    SeriesWordsKorolevaRecord(
                        level = level,
                        code = "pictureUrl",
                        exerciseName = exerciseName,
                        words = words,
                        playWordsCount = playWordsCount,
                        wordsColumns = wordsColumns
                    )
                )
            )
        }
    }

    private fun createExercise(): Exercise {
        val exercise = Exercise(
            name = exerciseName,
            playWordsCount = 1,
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
        )
    }

    private fun resource_бум(): Resource {
        return Resource(
            word = "бум",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/test/бум.ogg",
        )
    }

    private fun resource_быль(): Resource {
        return Resource(
            word = "быль",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/test/быль.ogg",
        )
    }

    private fun resource_вить(): Resource {
        return Resource(
            word = "вить",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/test/вить.ogg",
        )
    }

    private fun resource_гад(): Resource {
        return Resource(
            word = "гад",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/test/гад.ogg",
        )
    }

    private fun resource_дуб(): Resource {
        return Resource(
            word = "дуб",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/test/дуб.ogg",
        )
    }
}
