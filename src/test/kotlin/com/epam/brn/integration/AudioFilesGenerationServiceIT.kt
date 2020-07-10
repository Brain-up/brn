package com.epam.brn.integration

import com.epam.brn.service.AudioFilesGenerationService
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
@Disabled
internal class AudioFilesGenerationServiceIT {

    @Autowired
    lateinit var audioFilesGenerationService: AudioFilesGenerationService

    @Test
    fun `should get iam token from yandex cloud`() {
        // WHEN
        val iamToken = audioFilesGenerationService.getYandexIamTokenForAudioGeneration()
        val iamToken2 = audioFilesGenerationService.getYandexIamTokenForAudioGeneration()
        // THEN
        assertNotNull(iamToken)
        assertSame(iamToken, iamToken2)
    }

    @Test
    fun `should generate ogg audio file and then convert in to mp3 file`() {
        val voice = "alena"
        val resultFile1 = File("audio/$voice/бабушкааа.mp3")
        val resultFile2 = File("audio/$voice/доктор моет чёрные грушиии.mp3")
        // WHEN
        audioFilesGenerationService.generateAudioFile("бабушкааа", voice)
        audioFilesGenerationService.generateAudioFile("доктор моет чёрные грушиии", voice)
        // THEN
        assertTrue(resultFile1.exists())
        assertTrue(resultFile2.exists())
        resultFile1.deleteOnExit()
        resultFile2.deleteOnExit()
    }

    @Test
    fun `convert ogg file to mp3`() {
        val voice = "alena"
        val resultFile1 = File("audio/$voice/тест_привет.mp3")
        val resultFile2 = File("audio/$voice/тест_бабушка.mp3")
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
