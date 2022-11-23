package com.epam.brn.service.azure.tts

import com.epam.brn.repo.azure.tts.AzureVoiceInfoRepository
import com.epam.brn.service.azure.tts.config.AzureTtsProperties
import com.epam.brn.wiremock.BaseWireMockIT
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["default.tts.provider=azure"])
class AzureVoiceLoaderIT : BaseWireMockIT() {

    @Autowired
    private lateinit var azureTtsService: AzureTextToSpeechService

    @Autowired
    private lateinit var azureVoiceRepo: AzureVoiceInfoRepository

    @Autowired
    private lateinit var azureTtsProperties: AzureTtsProperties

    private lateinit var voiceLoader: AzureVoiceLoader

    @BeforeEach
    internal fun setUp() {
        azureVoiceRepo.deleteAll()
        voiceLoader = AzureVoiceLoader(azureTtsService, azureVoiceRepo, azureTtsProperties)
    }

    @AfterEach
    fun deleteAfterTest() {
        azureVoiceRepo.deleteAll()
    }

    @Test
    fun `should receive voices from Azure and save only accepted`() {
        // GIVEN
        stubForAllVoicesEndpoint()

        // WHEN
        voiceLoader.run()

        // THEN
        val voicesFromDb = azureVoiceRepo.findAll()
        voicesFromDb.size shouldBe 1

        val voice = voicesFromDb[0]
        voice.name shouldBe "Microsoft Server Speech Text to Speech Voice (en-US, AmberNeural)"
        voice.shortName shouldBe "en-US-AmberNeural"
        voice.displayName shouldBe "Amber"
        voice.localName shouldBe "Amber"
        voice.gender shouldBe "Female"
        voice.locale shouldBe "en-US"
        voice.localeName shouldBe "English (United States)"
        voice.sampleRateHertz shouldBe "24000"
        voice.voiceType shouldBe "Neural"
        voice.status shouldBe "GA"
    }

    private fun stubForAllVoicesEndpoint() {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("azure/tts/allVoices.json")
                )
        )
    }
}
