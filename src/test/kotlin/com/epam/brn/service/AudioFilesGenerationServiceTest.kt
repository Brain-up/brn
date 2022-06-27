package com.epam.brn.service

import com.epam.brn.service.cloud.AwsCloudService
import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.Locale
import com.epam.brn.enums.Voice
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.internal.assertSame
import org.apache.commons.codec.digest.DigestUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import java.io.File
import java.nio.file.Files

@ExtendWith(MockKExtension::class)
internal class AudioFilesGenerationServiceTest {

    @InjectMockKs
    lateinit var audioFilesGenerationService: AudioFilesGenerationService

    @MockK
    lateinit var wordsService: WordsService

    @MockK
    lateinit var yandexSpeechKitService: YandexSpeechKitService

    @MockK
    private lateinit var awsCloudService: AwsCloudService

    @Test
    fun `should process file`() {
        // GIVEN
        val fileMock = mockk<File>()
        val audioFileMetaData = AudioFileMetaData("word", "ru-ru", Voice.FILIPP.name)
        every { yandexSpeechKitService.generateAudioOggFile(audioFileMetaData) } returns fileMock

        // WHEN
        val actualResult = audioFilesGenerationService.processWord(audioFileMetaData)

        // THEN
        assertSame(fileMock, actualResult)
        verify(exactly = 0) { wordsService.getSubPathForWord(audioFileMetaData) }
        verify(exactly = 0) { awsCloudService.uploadFile(any(), any(), any()) }
    }

    @Test
    fun `should process file and save into cloud storage`() {
        // GIVEN
        val createTempFile = Files.createTempFile("", "")
        val file = createTempFile.toFile()
        file.writeBytes("SOMEDATA".toByteArray())
        val audioFileMetaData = AudioFileMetaData("word", "ru-ru", Voice.FILIPP.name)
        every { awsCloudService.uploadFile(any(), any(), any()) } returns Unit
        every { yandexSpeechKitService.generateAudioOggFile(audioFileMetaData) } returns file
        every { wordsService.getSubPathForWord(audioFileMetaData) } returns "/audio/${audioFileMetaData.locale}/${audioFileMetaData.voice.lowercase()}/${audioFileMetaData.speedFloat}"
        audioFilesGenerationService.withSavingToS3 = true
        // WHEN
        val actualResult = audioFilesGenerationService.processWord(audioFileMetaData)

        // THEN
        assertSame(file, actualResult)
        verify(exactly = 1) { wordsService.getSubPathForWord(audioFileMetaData) }
        verify(exactly = 1) { awsCloudService.uploadFile(any(), any(), any()) }
    }

    @Test
    fun `should process files`() {
        // GIVEN
        ReflectionTestUtils.setField(audioFilesGenerationService, "folderForLocalFiles", "testFolder")
        ReflectionTestUtils.setField(audioFilesGenerationService, "speeds", listOf("0.8", "1", "1.2"))
        ReflectionTestUtils.setField(audioFilesGenerationService, "withMp3Conversion", false)
        ReflectionTestUtils.setField(audioFilesGenerationService, "withSavingToS3", false)

        val fileMockOne1 = mockk<File>()
        val fileMockOne12 = mockk<File>()
        val fileMockOne08 = mockk<File>()
        val fileMockOne1En = mockk<File>()
        val fileMockOne12En = mockk<File>()
        val fileMockOne08En = mockk<File>()

        val wordTwoRu = "два"
        val wordOneEn = "one"
        val dictionaryByLocale = mutableMapOf(
            Locale.RU to mutableMapOf(wordTwoRu to DigestUtils.md5Hex(wordTwoRu)),
            Locale.EN to mutableMapOf(wordOneEn to DigestUtils.md5Hex(wordOneEn))
        )
        val metaTwoRu08M = AudioFileMetaData(wordTwoRu, Locale.RU.locale, Voice.FILIPP.name, "0.8")
        val metaTwoRu1M = AudioFileMetaData(wordTwoRu, Locale.RU.locale, Voice.FILIPP.name, "1")
        val metaTwoRu12M = AudioFileMetaData(wordTwoRu, Locale.RU.locale, Voice.FILIPP.name, "1.2")
        val metaTwoRu08W = AudioFileMetaData(wordTwoRu, Locale.RU.locale, Voice.OKSANA.name, "0.8")
        val metaTwoRu1W = AudioFileMetaData(wordTwoRu, Locale.RU.locale, Voice.OKSANA.name, "1")
        val metaTwoRu12W = AudioFileMetaData(wordTwoRu, Locale.RU.locale, Voice.OKSANA.name, "1.2")
        val metaOneEn08M = AudioFileMetaData(wordOneEn, Locale.EN.locale, Voice.NICK.name, "0.8")
        val metaOneEn1M = AudioFileMetaData(wordOneEn, Locale.EN.locale, Voice.NICK.name, "1")
        val metaOneEn12M = AudioFileMetaData(wordOneEn, Locale.EN.locale, Voice.NICK.name, "1.2")
        val metaOneEn08W = AudioFileMetaData(wordOneEn, Locale.EN.locale, Voice.ALYSS.name, "0.8")
        val metaOneEn1W = AudioFileMetaData(wordOneEn, Locale.EN.locale, Voice.ALYSS.name, "1")
        val metaOneEn12W = AudioFileMetaData(wordOneEn, Locale.EN.locale, Voice.ALYSS.name, "1.2")

        every { wordsService.dictionaryByLocale } returns dictionaryByLocale
        every { wordsService.getExistWordFilesCount(any()) } returns 1
        every { wordsService.isFileExistLocal(metaTwoRu08M) } returns false
        every { wordsService.isFileExistLocal(metaTwoRu1M) } returns false
        every { wordsService.isFileExistLocal(metaTwoRu12M) } returns false
        every { wordsService.isFileExistLocal(metaTwoRu08W) } returns true
        every { wordsService.isFileExistLocal(metaTwoRu1W) } returns true
        every { wordsService.isFileExistLocal(metaTwoRu12W) } returns true

        every { wordsService.isFileExistLocal(metaOneEn08M) } returns true
        every { wordsService.isFileExistLocal(metaOneEn1M) } returns true
        every { wordsService.isFileExistLocal(metaOneEn12M) } returns true
        every { wordsService.isFileExistLocal(metaOneEn08W) } returns false
        every { wordsService.isFileExistLocal(metaOneEn1W) } returns false
        every { wordsService.isFileExistLocal(metaOneEn12W) } returns false

        every { wordsService.getDefaultManVoiceForLocale(Locale.RU.locale) } returns Voice.FILIPP.name
        every { wordsService.getDefaultWomanVoiceForLocale(Locale.RU.locale) } returns Voice.OKSANA.name
        every { wordsService.getDefaultManVoiceForLocale(Locale.EN.locale) } returns Voice.NICK.name
        every { wordsService.getDefaultWomanVoiceForLocale(Locale.EN.locale) } returns Voice.ALYSS.name

        every { yandexSpeechKitService.generateAudioOggFile(metaTwoRu08M) } returns fileMockOne1
        every { yandexSpeechKitService.generateAudioOggFile(metaTwoRu1M) } returns fileMockOne08
        every { yandexSpeechKitService.generateAudioOggFile(metaTwoRu12M) } returns fileMockOne12

        every { yandexSpeechKitService.generateAudioOggFile(metaOneEn1W) } returns fileMockOne1En
        every { yandexSpeechKitService.generateAudioOggFile(metaOneEn08W) } returns fileMockOne08En
        every { yandexSpeechKitService.generateAudioOggFile(metaOneEn12W) } returns fileMockOne12En

        // WHEN
        audioFilesGenerationService.generateAudioFiles()

        // THEN
        Thread.sleep(1000)
        verify(exactly = 1) { yandexSpeechKitService.generateAudioOggFile(metaTwoRu08M) }
        verify(exactly = 1) { yandexSpeechKitService.generateAudioOggFile(metaTwoRu1M) }
        verify(exactly = 1) { yandexSpeechKitService.generateAudioOggFile(metaTwoRu12M) }
        verify(exactly = 1) { yandexSpeechKitService.generateAudioOggFile(metaOneEn1W) }
        verify(exactly = 1) { yandexSpeechKitService.generateAudioOggFile(metaOneEn08W) }
        verify(exactly = 1) { yandexSpeechKitService.generateAudioOggFile(metaOneEn12W) }
        confirmVerified(yandexSpeechKitService)
    }
}
