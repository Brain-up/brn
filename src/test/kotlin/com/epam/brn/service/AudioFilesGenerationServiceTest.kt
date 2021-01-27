package com.epam.brn.service

import com.epam.brn.config.AwsConfig
import com.nhaarman.mockito_kotlin.verify
import org.amshove.kluent.internal.assertSame
import org.apache.commons.codec.digest.DigestUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import java.io.File

@ExtendWith(MockitoExtension::class)
internal class AudioFilesGenerationServiceTest {

    @Mock
    lateinit var wordsService: WordsService

    @Mock
    lateinit var awsConfig: AwsConfig

    @Mock
    lateinit var yandexSpeechKitService: YandexSpeechKitService

    @InjectMocks
    lateinit var audioFilesGenerationService: AudioFilesGenerationService

    @Test
    fun `should process file`() {
        // GIVEN
        val fileMock = Mockito.mock(File::class.java)
        `when`(yandexSpeechKitService.generateAudioOggFile("word", "voice", "1")).thenReturn(fileMock)
        // WHEN
        val actualResult = audioFilesGenerationService.processWord("word", "voice", "1")
        // THEN
        assertSame(fileMock, actualResult)
    }

    @Test
    fun `should process files`() {
        // GIVEN
        ReflectionTestUtils.setField(audioFilesGenerationService, "speeds", listOf("0.8", "1", "1.2"))
        ReflectionTestUtils.setField(audioFilesGenerationService, "womanVoice", "alena")
        ReflectionTestUtils.setField(audioFilesGenerationService, "manVoice", "filipp")
        ReflectionTestUtils.setField(audioFilesGenerationService, "folderForFiles", "testFolder")
        ReflectionTestUtils.setField(audioFilesGenerationService, "withMp3Conversion", false)
        ReflectionTestUtils.setField(audioFilesGenerationService, "withSavingToS3", false)

        val fileMockOne1 = Mockito.mock(File::class.java)
        val fileMockOne12 = Mockito.mock(File::class.java)
        val fileMockOne08 = Mockito.mock(File::class.java)
        val fullWordsSet = hashSetOf("one", "two", "three")
        val existWords = hashSetOf(DigestUtils.md5Hex("two"), DigestUtils.md5Hex("three"))
        `when`(wordsService.fullWordsSet).thenReturn(fullWordsSet)
        `when`(wordsService.getExistWordFiles()).thenReturn(existWords)
        `when`(yandexSpeechKitService.generateAudioOggFile("one", "filipp", "1")).thenReturn(fileMockOne1)
        `when`(yandexSpeechKitService.generateAudioOggFile("one", "filipp", "0.8")).thenReturn(fileMockOne08)
        `when`(yandexSpeechKitService.generateAudioOggFile("one", "filipp", "1.2")).thenReturn(fileMockOne12)
        `when`(yandexSpeechKitService.generateAudioOggFile("one", "alena", "1")).thenReturn(fileMockOne1)
        `when`(yandexSpeechKitService.generateAudioOggFile("one", "alena", "0.8")).thenReturn(fileMockOne08)
        `when`(yandexSpeechKitService.generateAudioOggFile("one", "alena", "1.2")).thenReturn(fileMockOne12)
        // WHEN
        audioFilesGenerationService.generateAudioFiles()
        // THEN
        Thread.sleep(1000)
        verify(yandexSpeechKitService).generateAudioOggFile("one", "filipp", "1")
        verify(yandexSpeechKitService).generateAudioOggFile("one", "filipp", "0.8")
        verify(yandexSpeechKitService).generateAudioOggFile("one", "filipp", "1.2")
        verify(yandexSpeechKitService).generateAudioOggFile("one", "alena", "1")
        verify(yandexSpeechKitService).generateAudioOggFile("one", "alena", "0.8")
        verify(yandexSpeechKitService).generateAudioOggFile("one", "alena", "1.2")
    }
}
