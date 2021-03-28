package com.epam.brn.upload.csv.series1

import com.epam.brn.enums.AudiometryType
import com.epam.brn.enums.FrequencyZone
import com.epam.brn.enums.Locale
import com.epam.brn.enums.Voice
import com.epam.brn.model.Audiometry
import com.epam.brn.model.AudiometryTask
import com.epam.brn.model.Resource
import com.epam.brn.model.WordType
import com.epam.brn.repo.AudiometryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.service.WordsService
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
internal class LopotkoProcessorTest {

    private val audiometryRepository = mock(AudiometryRepository::class.java)
    private val audiometryTaskRepository = mock(AudiometryTaskRepository::class.java)
    private val resourceRepositoryMock = mock(ResourceRepository::class.java)
    private val wordsService = mock(WordsService::class.java)

    private lateinit var lopotkoRecordProcessor: LopotkoRecordProcessor

    private val words = listOf("(бал", "бум", "быль", "вить", "гад", "дуб)")

    private val audiometry =
        Audiometry(name = "Audiometry", audiometryType = AudiometryType.SPEECH.name, locale = Locale.RU.locale)
    private val audiometryTask = AudiometryTask(
        level = 1,
        audiometryGroup = "A",
        frequencyZone = FrequencyZone.LOW.name,
        minFrequency = 200,
        maxFrequency = 400,
    )
    private val savedAudiometryTask = audiometryTask.copy(id = 1)
    private val lopotkoRecord = LopotkoRecord(
        Locale.RU,
        AudiometryType.SPEECH.name,
        1,
        "A",
        FrequencyZone.LOW,
        200,
        400,
        words
    )

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    @BeforeEach
    internal fun setUp() {
        lopotkoRecordProcessor = LopotkoRecordProcessor(
            audiometryRepository,
            audiometryTaskRepository,
            resourceRepositoryMock,
            wordsService,
        )

        `when`(
            audiometryRepository.findByAudiometryTypeAndLocale(
                AudiometryType.SPEECH.name,
                Locale.RU.locale
            )
        ).thenReturn(audiometry)

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
    fun `should create correct audiometry task`() {
        // given
        `when`(audiometryTaskRepository.save(any(AudiometryTask::class.java))).thenReturn(savedAudiometryTask)
        `when`(wordsService.getDefaultManVoiceForLocale(Locale.RU.locale)).thenReturn(Voice.FILIPP)
        // when
        val actual = lopotkoRecordProcessor.process(mutableListOf(lopotkoRecord)).first()
        val expected = savedAudiometryTask
        assertThat(actual).isEqualToIgnoringGivenFields(expected, "id")
        verify(audiometryTaskRepository).save(any(AudiometryTask::class.java))
    }

    @Test
    fun `should create correct answer options`() {
        // given
        val resources = mutableSetOf(
            resource_бал(),
            resource_бум(),
            resource_быль(),
            resource_вить(),
            resource_гад(),
            resource_дуб()
        )
        val audiometryTaskWithResources = savedAudiometryTask.copy(answerOptions = resources)
        `when`(audiometryTaskRepository.save(any(AudiometryTask::class.java))).thenReturn(audiometryTaskWithResources)
        `when`(wordsService.getDefaultManVoiceForLocale(Locale.RU.locale)).thenReturn(Voice.FILIPP)
        // when
        val actualtask = lopotkoRecordProcessor.process(mutableListOf(lopotkoRecord)).first()
        // then
        assertThat(actualtask.answerOptions).containsExactlyElementsOf(resources)
    }

    private fun resource_бал(): Resource {
        return Resource(
            word = "бал",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/audio/filipp/518d3c4523afcd59e2feae1093870f5f.ogg",
            pictureFileUrl = "pictures/бал.jpg"
        )
    }

    private fun resource_бум(): Resource {
        return Resource(
            word = "бум",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/audio/filipp/8e3cba18a3a6a3aa51e160a3d1e1ebcc.ogg",
            pictureFileUrl = "pictures/бум.jpg"
        )
    }

    private fun resource_быль(): Resource {
        return Resource(
            word = "быль",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/audio/filipp/4df3cdbbe2abf27f91f673032c95141e.ogg",
            pictureFileUrl = "pictures/быль.jpg"
        )
    }

    private fun resource_вить(): Resource {
        return Resource(
            word = "вить",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/audio/filipp/77ebaea90791bb15d4f758191aae5930.ogg",
            pictureFileUrl = "pictures/вить.jpg"
        )
    }

    private fun resource_гад(): Resource {
        return Resource(
            word = "гад",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/audio/filipp/2e0b56e224fe469866e1aaa81caaafcc.ogg",
            pictureFileUrl = "pictures/гад.jpg"
        )
    }

    private fun resource_дуб(): Resource {
        return Resource(
            word = "дуб",
            wordType = WordType.OBJECT.toString(),
            audioFileUrl = "/audio/filipp/494d676049e14da7fd3a9182955287ab.ogg",
            pictureFileUrl = "pictures/дуб.jpg"
        )
    }
}
