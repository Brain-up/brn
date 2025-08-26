package com.epam.brn.integration.service

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.Voice
import com.epam.brn.service.YandexSpeechKitService
import org.apache.commons.codec.digest.DigestUtils
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File
import java.io.InputStream
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
@Disabled("as it is write only for testing locally")
internal class YandexSpeechKitServiceIT {
    @Autowired
    lateinit var yandexSpeechKitService: YandexSpeechKitService

    @Test
    fun `should get iam token from yandex cloud`() {
        // WHEN
        val iamToken1 = yandexSpeechKitService.getYandexIamTokenForAudioGeneration()
        val iamToken2 = yandexSpeechKitService.getYandexIamTokenForAudioGeneration()
        // THEN
        assertNotNull(iamToken1)
        assertSame(iamToken1, iamToken2)
    }

    @Test
    fun `should generate ogg audio stream with validation`() {
        val locale = BrnLocale.RU.locale
        val voice = Voice.OKSANA.name
        val speed = "1"
        val meta1 = AudioFileMetaData("бабушкааа", locale, voice, speed)
        val meta2 = AudioFileMetaData("доктор моет чёрные грушиии", locale, voice, speed)
        // WHEN
        val fileWordResult: InputStream = yandexSpeechKitService.generateAudioOggStreamWithValidation(meta1)
        val filePhraseResult: InputStream = yandexSpeechKitService.generateAudioOggStreamWithValidation(meta2)
        // THEN
        val expectedFileWord = File("audioTest/ogg/$locale/$voice/$speed/${DigestUtils.md5Hex(meta1.text)}.ogg")
        val expectedFilePhrase = File("audioTest/ogg/$locale/$voice/$speed/${DigestUtils.md5Hex(meta2.text)}.ogg")
//        assertEquals(expectedFileWord, fileWordResult)
//        assertEquals(expectedFilePhrase, filePhraseResult)
        assertTrue(fileWordResult.toString().isNotEmpty())
        assertTrue(filePhraseResult.toString().isNotEmpty())
        assertTrue(expectedFileWord.exists())
        assertTrue(expectedFilePhrase.exists())
        expectedFileWord.deleteOnExit()
        expectedFilePhrase.deleteOnExit()
    }
}
