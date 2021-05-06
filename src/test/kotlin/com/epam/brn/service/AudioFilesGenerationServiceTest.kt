package com.epam.brn.service

import com.epam.brn.config.AwsConfig
import com.epam.brn.enums.Locale
import com.epam.brn.enums.Voice
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
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
        val audioFileMetaData = AudioFileMetaData("word", "ru-ru", Voice.FILIPP)
        `when`(yandexSpeechKitService.generateAudioOggFile(audioFileMetaData)).thenReturn(fileMock)
        // WHEN
        val actualResult = audioFilesGenerationService.processWord(audioFileMetaData)
        // THEN
        assertSame(fileMock, actualResult)
    }

    @Test
    fun `should process files`() {
        // GIVEN
        ReflectionTestUtils.setField(audioFilesGenerationService, "folderForLocalFiles", "testFolder")
        ReflectionTestUtils.setField(audioFilesGenerationService, "speeds", listOf("0.8", "1", "1.2"))
        ReflectionTestUtils.setField(audioFilesGenerationService, "withMp3Conversion", false)
        ReflectionTestUtils.setField(audioFilesGenerationService, "withSavingToS3", false)

        val fileMockOne1 = Mockito.mock(File::class.java)
        val fileMockOne12 = Mockito.mock(File::class.java)
        val fileMockOne08 = Mockito.mock(File::class.java)
        val fileMockOne1En = Mockito.mock(File::class.java)
        val fileMockOne12En = Mockito.mock(File::class.java)
        val fileMockOne08En = Mockito.mock(File::class.java)

        val wordTwoRu = "два"
        val wordOneEn = "one"
        val dictionaryByLocale = mutableMapOf(
            Locale.RU to mutableMapOf(wordTwoRu to DigestUtils.md5Hex(wordTwoRu)),
            Locale.EN to mutableMapOf(wordOneEn to DigestUtils.md5Hex(wordOneEn))
        )
        val metaTwoRu08M = AudioFileMetaData(wordTwoRu, Locale.RU.locale, Voice.FILIPP, "0.8")
        val metaTwoRu1M = AudioFileMetaData(wordTwoRu, Locale.RU.locale, Voice.FILIPP, "1")
        val metaTwoRu12M = AudioFileMetaData(wordTwoRu, Locale.RU.locale, Voice.FILIPP, "1.2")
        val metaTwoRu08W = AudioFileMetaData(wordTwoRu, Locale.RU.locale, Voice.OKSANA, "0.8")
        val metaTwoRu1W = AudioFileMetaData(wordTwoRu, Locale.RU.locale, Voice.OKSANA, "1")
        val metaTwoRu12W = AudioFileMetaData(wordTwoRu, Locale.RU.locale, Voice.OKSANA, "1.2")
        val metaOneEn08M = AudioFileMetaData(wordOneEn, Locale.EN.locale, Voice.NICK, "0.8")
        val metaOneEn1M = AudioFileMetaData(wordOneEn, Locale.EN.locale, Voice.NICK, "1")
        val metaOneEn12M = AudioFileMetaData(wordOneEn, Locale.EN.locale, Voice.NICK, "1.2")
        val metaOneEn08W = AudioFileMetaData(wordOneEn, Locale.EN.locale, Voice.ALYSS, "0.8")
        val metaOneEn1W = AudioFileMetaData(wordOneEn, Locale.EN.locale, Voice.ALYSS, "1")
        val metaOneEn12W = AudioFileMetaData(wordOneEn, Locale.EN.locale, Voice.ALYSS, "1.2")

        `when`(wordsService.dictionaryByLocale).thenReturn(dictionaryByLocale)
        `when`(wordsService.isFileExistLocal(metaTwoRu08M)).thenReturn(false)
        `when`(wordsService.isFileExistLocal(metaTwoRu1M)).thenReturn(false)
        `when`(wordsService.isFileExistLocal(metaTwoRu12M)).thenReturn(false)
        `when`(wordsService.isFileExistLocal(metaTwoRu08W)).thenReturn(true)
        `when`(wordsService.isFileExistLocal(metaTwoRu1W)).thenReturn(true)
        `when`(wordsService.isFileExistLocal(metaTwoRu12W)).thenReturn(true)

        `when`(wordsService.isFileExistLocal(metaOneEn08M)).thenReturn(true)
        `when`(wordsService.isFileExistLocal(metaOneEn1M)).thenReturn(true)
        `when`(wordsService.isFileExistLocal(metaOneEn12M)).thenReturn(true)
        `when`(wordsService.isFileExistLocal(metaOneEn08W)).thenReturn(false)
        `when`(wordsService.isFileExistLocal(metaOneEn1W)).thenReturn(false)
        `when`(wordsService.isFileExistLocal(metaOneEn12W)).thenReturn(false)

        `when`(wordsService.getDefaultManVoiceForLocale(Locale.RU.locale)).thenReturn(Voice.FILIPP)
        `when`(wordsService.getDefaultWomanVoiceForLocale(Locale.RU.locale)).thenReturn(Voice.OKSANA)
        `when`(wordsService.getDefaultManVoiceForLocale(Locale.EN.locale)).thenReturn(Voice.NICK)
        `when`(wordsService.getDefaultWomanVoiceForLocale(Locale.EN.locale)).thenReturn(Voice.ALYSS)

        `when`(yandexSpeechKitService.generateAudioOggFile(metaTwoRu08M)).thenReturn(fileMockOne1)
        `when`(yandexSpeechKitService.generateAudioOggFile(metaTwoRu1M)).thenReturn(fileMockOne08)
        `when`(yandexSpeechKitService.generateAudioOggFile(metaTwoRu12M)).thenReturn(fileMockOne12)

        `when`(yandexSpeechKitService.generateAudioOggFile(metaOneEn1W)).thenReturn(fileMockOne1En)
        `when`(yandexSpeechKitService.generateAudioOggFile(metaOneEn08W)).thenReturn(fileMockOne08En)
        `when`(yandexSpeechKitService.generateAudioOggFile(metaOneEn12W)).thenReturn(fileMockOne12En)

        // WHEN
        audioFilesGenerationService.generateAudioFiles()

        // THEN
        Thread.sleep(1000)
        verify(yandexSpeechKitService).generateAudioOggFile(metaTwoRu08M)
        verify(yandexSpeechKitService).generateAudioOggFile(metaTwoRu1M)
        verify(yandexSpeechKitService).generateAudioOggFile(metaTwoRu12M)
        verify(yandexSpeechKitService).generateAudioOggFile(metaOneEn1W)
        verify(yandexSpeechKitService).generateAudioOggFile(metaOneEn08W)
        verify(yandexSpeechKitService).generateAudioOggFile(metaOneEn12W)
        verifyNoMoreInteractions(yandexSpeechKitService)
    }
}
