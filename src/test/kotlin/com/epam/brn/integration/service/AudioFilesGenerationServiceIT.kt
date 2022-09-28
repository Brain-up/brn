package com.epam.brn.integration.service

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.Voice
import com.epam.brn.service.AudioFilesGenerationService
import org.apache.commons.codec.digest.DigestUtils
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import java.io.File
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
@Disabled("as it is write only for testing locally")
internal class AudioFilesGenerationServiceIT {

    @Autowired
    lateinit var audioFilesGenerationService: AudioFilesGenerationService

    @Test
    fun `should generate ogg audio file and then convert it into mp3 file`() {
        ReflectionTestUtils.setField(audioFilesGenerationService, "withMp3Conversion", true)
        ReflectionTestUtils.setField(audioFilesGenerationService, "withSavingToS3", false)

        val locale = BrnLocale.RU.locale
        val voice = Voice.OKSANA.name
        val speed = "1"
        val meta1 = AudioFileMetaData("бабушкааа", locale, voice, speed)
        val meta2 = AudioFileMetaData("доктор моет чёрные грушиии", locale, voice, speed)

        val resultFile1Ogg = File("audioTest/ogg/$voice/${DigestUtils.md5Hex("бабушкааа")}.ogg")
        val resultFile1Mp3 = File("audioTest/$voice/${DigestUtils.md5Hex("бабушкааа")}.mp3")
        val resultFile2Ogg = File("audioTest/ogg/$voice/${DigestUtils.md5Hex("доктор моет чёрные грушиии")}.ogg")
        val resultFile2Mp3 = File("audioTest/$voice/${DigestUtils.md5Hex("доктор моет чёрные грушиии")}.mp3")

        // WHEN
        audioFilesGenerationService.processWord(meta1)
        audioFilesGenerationService.processWord(meta2)
        // THEN
        assertTrue(resultFile1Ogg.exists())
        assertTrue(resultFile1Mp3.exists())
        assertTrue(resultFile2Ogg.exists())
        assertTrue(resultFile2Mp3.exists())
        resultFile1Ogg.deleteOnExit()
        resultFile1Mp3.deleteOnExit()
        resultFile2Ogg.deleteOnExit()
        resultFile2Mp3.deleteOnExit()
    }

    @Test
    fun `convert ogg file to mp3`() {
        val voice = "alena"
        val resultFile1 = File("audioTest/$voice/тест_привет.mp3")
        val resultFile2 = File("audioTest/$voice/тест_бабушка.mp3")
        // WHEN
        audioFilesGenerationService.convertOggFileToMp3(File("testAudioFiles/тест_привет.ogg"), voice)
        audioFilesGenerationService.convertOggFileToMp3(File("testAudioFiles/тест_бабушка.ogg"), voice)
        // THEN
        assertTrue(resultFile1.exists())
        assertTrue(resultFile2.exists())
        resultFile1.deleteOnExit()
        resultFile2.deleteOnExit()
    }
}
