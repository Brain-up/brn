package com.epam.brn.upload.csv.audiometrySpeech

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.AudiometryType
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.FrequencyZone
import com.epam.brn.enums.Voice
import com.epam.brn.enums.WordType
import com.epam.brn.model.Audiometry
import com.epam.brn.model.AudiometryTask
import com.epam.brn.model.Resource
import com.epam.brn.repo.AudiometryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import com.epam.brn.repo.ResourceRepository
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
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class LopotkoRecordProcessorTest {
    @MockK
    private lateinit var audiometryRepository: AudiometryRepository

    @MockK
    private lateinit var audiometryTaskRepository: AudiometryTaskRepository

    @MockK
    private lateinit var resourceRepositoryMock: ResourceRepository

    @MockK
    private lateinit var wordsService: WordsService

    @InjectMockKs
    private lateinit var lopotkoRecordProcessor: LopotkoRecordProcessor

    private val words = listOf("(бал", "бум", "быль", "вить", "гад", "дуб)")

    private val audiometry =
        Audiometry(name = "Audiometry", audiometryType = AudiometryType.SPEECH.name, locale = BrnLocale.RU.locale)
    private val audiometryTask =
        AudiometryTask(
            level = 1,
            audiometryGroup = "A",
            frequencyZone = FrequencyZone.LOW.name,
            minFrequency = 200,
            maxFrequency = 400,
        )
    private val savedAudiometryTask =
        AudiometryTask(
            id = 1,
            level = 1,
            audiometryGroup = "A",
            frequencyZone = FrequencyZone.LOW.name,
            minFrequency = 200,
            maxFrequency = 400,
        )
    private val lopotkoRecord =
        LopotkoRecord(
            BrnLocale.RU,
            AudiometryType.SPEECH.name,
            1,
            "A",
            FrequencyZone.LOW,
            200,
            400,
            words,
        )

    @BeforeEach
    internal fun setUp() {
        lopotkoRecordProcessor =
            LopotkoRecordProcessor(
                audiometryRepository,
                audiometryTaskRepository,
                resourceRepositoryMock,
                wordsService,
            )

        every { wordsService.getDefaultManVoiceForLocale(BrnLocale.RU.locale) } returns Voice.FILIPP.name
        every { wordsService.getSubFilePathForWord(ofType(AudioFileMetaData::class)) } returns String()
        every {
            audiometryRepository.findByAudiometryTypeAndLocale(
                AudiometryType.SPEECH.name,
                BrnLocale.RU.locale,
            )
        } returns audiometry
        every {
            resourceRepositoryMock.findFirstByWordAndWordTypeAndAudioFileUrlLike(
                ofType(String::class),
                ofType(String::class),
                ofType(String::class),
            )
        } returns Optional.empty()
        every { resourceRepositoryMock.saveAll(any<List<Resource>>()) } returns emptySet()
        every {
            audiometryTaskRepository.findByAudiometryAndFrequencyZoneAndAudiometryGroup(
                ofType(Audiometry::class),
                ofType(String::class),
                ofType(String::class),
            )
        } returns null

        mockFindResourceByWordLike("бал", resource_бал())
        mockFindResourceByWordLike("бум", resource_бум())
        mockFindResourceByWordLike("быль", resource_быль())
        mockFindResourceByWordLike("вить", resource_вить())
        mockFindResourceByWordLike("гад", resource_гад())
        mockFindResourceByWordLike("дуб", resource_дуб())
    }

    private fun mockFindResourceByWordLike(
        word: String,
        result: Resource,
    ) {
        every { resourceRepositoryMock.findFirstByWordLike(word) } returns Optional.of(result)
    }

    @Test
    fun `should create correct audiometry task`() {
        // given
        every { audiometryTaskRepository.save(ofType(AudiometryTask::class)) } returns savedAudiometryTask
        // when
        val actual = lopotkoRecordProcessor.process(mutableListOf(lopotkoRecord)).first()
        val expected = savedAudiometryTask
        assertThat(actual).isEqualTo(expected)
        verify { audiometryTaskRepository.save(ofType(AudiometryTask::class)) }
    }

    @Test
    fun `should create correct answer options`() {
        // given
        val resources =
            mutableSetOf(
                resource_бал(),
                resource_бум(),
                resource_быль(),
                resource_вить(),
                resource_гад(),
                resource_дуб(),
            )
        val audiometryTaskWithResources =
            AudiometryTask(
                id = 1,
                level = 1,
                audiometryGroup = "A",
                frequencyZone = FrequencyZone.LOW.name,
                minFrequency = 200,
                maxFrequency = 400,
                answerOptions = resources,
            )
        every { audiometryTaskRepository.save(ofType(AudiometryTask::class)) } returns audiometryTaskWithResources
        // when
        val actualtask = lopotkoRecordProcessor.process(mutableListOf(lopotkoRecord)).first()
        // then
        assertThat(actualtask.answerOptions).containsExactlyElementsOf(resources)
    }

    private fun resource_бал(): Resource = Resource(
        word = "бал",
        wordType = WordType.OBJECT.toString(),
        audioFileUrl = "/audio/filipp/518d3c4523afcd59e2feae1093870f5f.ogg",
        pictureFileUrl = "pictures/бал.jpg",
    )

    private fun resource_бум(): Resource = Resource(
        word = "бум",
        wordType = WordType.OBJECT.toString(),
        audioFileUrl = "/audio/filipp/8e3cba18a3a6a3aa51e160a3d1e1ebcc.ogg",
        pictureFileUrl = "pictures/бум.jpg",
    )

    private fun resource_быль(): Resource = Resource(
        word = "быль",
        wordType = WordType.OBJECT.toString(),
        audioFileUrl = "/audio/filipp/4df3cdbbe2abf27f91f673032c95141e.ogg",
        pictureFileUrl = "pictures/быль.jpg",
    )

    private fun resource_вить(): Resource = Resource(
        word = "вить",
        wordType = WordType.OBJECT.toString(),
        audioFileUrl = "/audio/filipp/77ebaea90791bb15d4f758191aae5930.ogg",
        pictureFileUrl = "pictures/вить.jpg",
    )

    private fun resource_гад(): Resource = Resource(
        word = "гад",
        wordType = WordType.OBJECT.toString(),
        audioFileUrl = "/audio/filipp/2e0b56e224fe469866e1aaa81caaafcc.ogg",
        pictureFileUrl = "pictures/гад.jpg",
    )

    private fun resource_дуб(): Resource = Resource(
        word = "дуб",
        wordType = WordType.OBJECT.toString(),
        audioFileUrl = "/audio/filipp/494d676049e14da7fd3a9182955287ab.ogg",
        pictureFileUrl = "pictures/дуб.jpg",
    )
}
