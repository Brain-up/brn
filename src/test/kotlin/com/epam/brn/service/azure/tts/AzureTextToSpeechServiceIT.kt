package com.epam.brn.service.azure.tts

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.repo.azure.tts.AzureVoiceInfoRepository
import com.epam.brn.wiremock.BaseWireMockIT
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional

@TestPropertySource(properties = ["default.tts.provider=azure"])
class AzureTextToSpeechServiceIT : BaseWireMockIT() {
    @Autowired
    private lateinit var service: AzureTextToSpeechService

    @Autowired
    private lateinit var azureVoiceRepo: AzureVoiceInfoRepository

    private val params =
        AudioFileMetaData(
            voice = "af-ZA-AdriNeural",
            gender = "Female",
            locale = "af-ZA",
            text = "text",
        )
    private val audioData = byteArrayOf(10, 20, 30, 40, 50)

    @AfterEach
    internal fun tearDown() {
        azureVoiceRepo.deleteAll()
    }

    @Test
    fun `should return all voices`() {
        // GIVEN
        stubForAllVoicesEndpoint()

        // WHEN
        val voices = service.getVoices()

        // THEN
        voices shouldNotBe null
        voices.size shouldBe 5
    }

    @Test
    fun `should return particular voice`() {
        // GIVEN
        stubForAllVoicesEndpoint()
        stubForTtsEndpoint()

        azureVoiceRepo.saveAll(service.getVoices().map { voice -> voice.convertToEntity() })

        // WHEN
        val voice = service.getVoiceInfo(params)

        // THEN
        voice.shortName shouldBe params.voice
        voice.gender shouldBe params.gender
        voice.displayName shouldBe "Adri"
        voice.localName shouldBe "Adri"
        voice.localeName shouldBe "Afrikaans (South Africa)"
    }

    @Test
    @Transactional
    fun `should call Azure text to speech endpoint`() {
        // GIVEN
        stubForAllVoicesEndpoint()
        stubForTtsEndpoint()

        azureVoiceRepo.saveAll(service.getVoices().map { voice -> voice.convertToEntity() })

        // WHEN
        val audioStream = service.textToSpeech(params)

        // THEN
        audioStream.readBytes() shouldBe audioData
    }

    private fun stubForAllVoicesEndpoint() {
        stubFor(
            get(urlPathMatching(".*"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("azure/tts/allVoices.json"),
                ),
        )
    }

    private fun stubForTtsEndpoint() {
        stubFor(
            post((urlPathMatching(".*")))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, "audio/mpeg")
                        .withBody(audioData),
                ),
        )
    }
}
