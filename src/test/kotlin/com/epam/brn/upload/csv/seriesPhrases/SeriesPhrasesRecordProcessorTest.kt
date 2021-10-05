package com.epam.brn.upload.csv.seriesPhrases

import com.epam.brn.enums.Locale
import com.epam.brn.enums.Voice
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.SubGroup
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.service.WordsService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class SeriesPhrasesRecordProcessorTest {

    @InjectMockKs
    private lateinit var seriesPhrasesRecordProcessor: SeriesPhrasesRecordProcessor

    @MockK
    private lateinit var exerciseRepository: ExerciseRepository

    @MockK
    private lateinit var subGroupRepository: SubGroupRepository

    @MockK
    private lateinit var resourceRepository: ResourceRepository

    @MockK
    private lateinit var wordsService: WordsService

    @BeforeEach
    fun setup() {
        ReflectionTestUtils.setField(seriesPhrasesRecordProcessor, "pictureDefaultPath", "pictures/%s.jpg")
    }

    @Test
    fun `should create correct exercise`() {
        // GIVEN
        val seriesPhrasesRecord = SeriesPhrasesRecord(
            level = 1,
            code = "subgroup code",
            exerciseName = "exercise name",
            phrases = listOf("some text.", "next text"),
            noiseLevel = 0,
            noiseUrl = ""
        )
        val subGroup = mockk<SubGroup>()
        val exercise = mockk<Exercise>()
        every { exerciseRepository.findExerciseByNameAndLevel(any(), any()) } returns Optional.empty()
        every { subGroupRepository.findByCodeAndLocale(any(), any()) } returns subGroup
        every { wordsService.getDefaultManVoiceForLocale(any()) } returns Voice.FILIPP
        every { wordsService.getSubFilePathForWord(any()) } returns ""
        every { resourceRepository.findFirstByWordAndLocaleAndWordType(any(), any(), any()) } returns Optional.empty()
        every { wordsService.addWordsToDictionary(any(), any()) } returns Unit
        every { resourceRepository.saveAll(any<List<Resource>>()) } returns emptyList()
        every { exerciseRepository.save(any()) } returns exercise

        // WHEN
        val exercises = seriesPhrasesRecordProcessor.process(listOf(seriesPhrasesRecord), Locale.RU)

        // THEN
        verify(exactly = 1) { exerciseRepository.findExerciseByNameAndLevel(any(), any()) }
        verify(exactly = 1) { subGroupRepository.findByCodeAndLocale(any(), any()) }
        verify(exactly = 2) { wordsService.getDefaultManVoiceForLocale(any()) }
        verify(exactly = 2) { wordsService.getSubFilePathForWord(any()) }
        verify(exactly = 2) { resourceRepository.findFirstByWordAndLocaleAndWordType(any(), any(), any()) }
        verify(exactly = 1) { wordsService.addWordsToDictionary(any(), any()) }
        verify(exactly = 1) { resourceRepository.saveAll(any<List<Resource>>()) }
        verify(exactly = 1) { exerciseRepository.save(any()) }
        exercises shouldHaveSize 1
        exercises[0].name shouldBe seriesPhrasesRecord.exerciseName
        exercises[0].tasks shouldHaveSize 1
        exercises[0].level shouldBe seriesPhrasesRecord.level
        exercises[0].subGroup shouldNotBe null
    }

    @Test
    fun `should not create exercise because exercise exist`() {
        // GIVEN
        val seriesPhrasesRecord = SeriesPhrasesRecord(
            level = 1,
            code = "subgroup code",
            exerciseName = "exercise name",
            phrases = listOf("some text.", "next text"),
            noiseLevel = 0,
            noiseUrl = ""
        )
        val subGroupMock = mockk<SubGroup>()
        val exerciseMock = mockk<Exercise>()
        every { subGroupRepository.findByCodeAndLocale(any(), any()) } returns subGroupMock
        every { exerciseRepository.findExerciseByNameAndLevel(any(), any()) } returns Optional.of(exerciseMock)

        // WHEN
        val exercises = seriesPhrasesRecordProcessor.process(listOf(seriesPhrasesRecord), Locale.RU)

        // THEN
        verify(exactly = 1) { subGroupRepository.findByCodeAndLocale(any(), any()) }
        verify(exactly = 1) { exerciseRepository.findExerciseByNameAndLevel(any(), any()) }
        exercises.shouldBeEmpty()
    }

    @Test
    fun `should throw EntityNotFoundException`() {
        // GIVEN
        val seriesPhrasesRecord = SeriesPhrasesRecord(
            level = 1,
            code = "subgroup code",
            exerciseName = "exercise name",
            phrases = listOf("some text.", "next text"),
            noiseLevel = 0,
            noiseUrl = ""
        )
        val locale = Locale.RU
        every { subGroupRepository.findByCodeAndLocale(any(), any()) } returns null

        // WHEN
        val exception = shouldThrow<EntityNotFoundException> {
            seriesPhrasesRecordProcessor.process(listOf(seriesPhrasesRecord), locale)
        }

        // THEN
        verify(exactly = 1) { subGroupRepository.findByCodeAndLocale(any(), any()) }
        exception.message shouldBe "No subGroup was found for code=${seriesPhrasesRecord.code} and locale={${locale.locale}}"
    }
}
