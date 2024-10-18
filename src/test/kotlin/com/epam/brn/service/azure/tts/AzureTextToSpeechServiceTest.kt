package com.epam.brn.service.azure.tts

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.model.azure.tts.AzureVoiceInfo
import com.epam.brn.repo.azure.tts.AzureVoiceInfoRepository
import com.epam.brn.service.WordsService
import com.epam.brn.service.azure.tts.config.AzureTtsProperties
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.reactive.function.client.WebClient
import java.io.ByteArrayInputStream

@ExtendWith(MockKExtension::class)
class AzureTextToSpeechServiceTest {

    @SpyK
    @InjectMockKs
    lateinit var ttsService: AzureTextToSpeechService

    @MockK
    lateinit var wordsService: WordsService

    @MockK
    lateinit var azureTtsWebClient: WebClient

    @MockK
    lateinit var azureAllVoicesWebClient: WebClient

    @MockK
    lateinit var azureVoiceRepo: AzureVoiceInfoRepository

    @MockK
    lateinit var azureTtsProperties: AzureTtsProperties

    private val params = AudioFileMetaData(
        voice = "en-US-ChristopherNeural",
        gender = "Male",
        locale = "en-US",
        text = "text",
    )

    @Test
    fun `should find voice by voice name`() {
        // GIVEN
        val voiceFromDb = AzureVoiceInfo(shortName = params.voice, gender = "Male", locale = params.locale)
        every { azureVoiceRepo.findByShortName(params.voice) } returns voiceFromDb

        // WHEN
        val voiceInfo = ttsService.getVoiceInfo(params)

        // THEN
        verify { azureVoiceRepo.findByShortName(params.voice) }
        verify(exactly = 0) { azureVoiceRepo.findByLocaleIgnoreCaseAndGenderIgnoreCase(any(), any()) }

        voiceInfo shouldBe voiceFromDb
    }

    @Test
    fun `should find voice by locale and gender if voice name not provided`() {
        // GIVEN
        val voiceFromDb = AzureVoiceInfo(shortName = params.voice, gender = "Male", locale = params.locale)
        every { azureVoiceRepo.findByShortName(params.voice) } returns null
        every {
            azureVoiceRepo.findByLocaleIgnoreCaseAndGenderIgnoreCase(params.locale, params.gender!!)
        } returns mutableListOf(voiceFromDb)

        // WHEN
        val voiceInfo = ttsService.getVoiceInfo(params)

        // THEN
        verify { azureVoiceRepo.findByShortName(params.voice) }
        verify { azureVoiceRepo.findByLocaleIgnoreCaseAndGenderIgnoreCase(params.locale, params.gender!!) }

        voiceInfo shouldBe voiceFromDb
    }

    @Test
    fun `should get voice info from props if not found in DB`() {
        // GIVEN
        every { azureVoiceRepo.findByShortName(params.voice) } returns null
        every {
            azureVoiceRepo.findByLocaleIgnoreCaseAndGenderIgnoreCase(params.locale, params.gender!!)
        } returns mutableListOf()

        every { azureTtsProperties.defaultVoiceName } returns "defaultVoiceName"
        every { azureTtsProperties.defaultGender } returns "defaultGender"
        every { azureTtsProperties.defaultLang } returns "defaultLang"

        // WHEN
        val voiceInfo = ttsService.getVoiceInfo(params)

        // THEN
        verify { azureVoiceRepo.findByShortName(params.voice) }
        verify { azureVoiceRepo.findByLocaleIgnoreCaseAndGenderIgnoreCase(params.locale, params.gender!!) }

        voiceInfo.shortName shouldBe "defaultVoiceName"
        voiceInfo.gender shouldBe "defaultGender"
        voiceInfo.locale shouldBe "defaultLang"
    }

    @Test
    fun `should generate xml request to TTS Azure service`() {
        // GIVEN
        val voiceFromDb = AzureVoiceInfo(shortName = params.voice, gender = "Male", locale = params.locale)
        every { azureVoiceRepo.findByShortName(params.voice) } returns voiceFromDb

        // WHEN
        val textToSpeechRequest = ttsService.getTextToSpeechRequest(params)

        textToSpeechRequest shouldBe """
            <speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xmlns:mstts="https://www.w3.org/2001/mstts" xml:lang="en-US"><voice name="en-US-ChristopherNeural" xml:lang="en-US" xml:gender="Male"><prosody pitch="default" rate="default"><mstts:express-as styledegree="1">text</mstts:express-as></prosody></voice></speak>
        """.trimIndent()

        // THEN
        verify { azureVoiceRepo.findByShortName(params.voice) }
    }

    @Test
    fun `should call textToSpeech method when calling generateAudioOggFileWithValidation`() {
        // GIVEN
        val text = "text"
        val locale = "locale"
        val voice = "voice"
        val speed = "speed"
        val gender = "gender"
        val pitch = "pitch"
        val style = "style"

        val audioFileMetaData = AudioFileMetaData(
            text = text,
            locale = locale,
            voice = voice,
            gender = gender,
            speedFloat = speed,
            pitch = pitch,
            style = style
        )

        val audioBytes = "audio-input-stream".toByteArray()
        val mockInputStream = ByteArrayInputStream(audioBytes)
        every { ttsService.textToSpeech(audioFileMetaData) } returns mockInputStream

        // WHEN
        val file = ttsService.generateAudioOggStreamWithValidation(audioFileMetaData)

        // THEN
        file.readBytes() shouldBe audioBytes
        verify { ttsService.textToSpeech(audioFileMetaData) }
    }
}
