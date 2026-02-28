package com.epam.brn.service

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.dto.YandexIamTokenDto
import com.epam.brn.dto.yandex.tts.AudioChunk
import com.epam.brn.dto.yandex.tts.YandexTtsResponse
import com.epam.brn.dto.yandex.tts.YandexTtsResult
import com.epam.brn.exception.YandexServiceException
import com.epam.brn.service.yandex.tts.config.YandexTtsProperties
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.Base64

internal class YandexSpeechKitServiceTest {
    private lateinit var yandexSpeechKitService: YandexSpeechKitService
    private lateinit var wordsService: WordsService
    private lateinit var timeService: TimeService
    private lateinit var yandexTtsProperties: YandexTtsProperties
    private lateinit var yandexTtsWebClient: WebClient
    private lateinit var yandexIamTokenWebClient: WebClient

    @BeforeEach
    fun setUp() {
        wordsService = mockk()
        timeService = mockk()
        yandexTtsProperties = mockk()
        yandexTtsWebClient = mockk()
        yandexIamTokenWebClient = mockk()

        every { yandexTtsProperties.emotions } returns listOf("friendly")
        every { yandexTtsProperties.folderId } returns "test-folder-id"
        every { yandexTtsProperties.authToken } returns "test-auth-token"

        yandexSpeechKitService =
            YandexSpeechKitService(
                wordsService = wordsService,
                timeService = timeService,
                yandexTtsProperties = yandexTtsProperties,
                yandexTtsWebClient = yandexTtsWebClient,
                yandexIamTokenWebClient = yandexIamTokenWebClient,
            )
    }

    @ParameterizedTest
    @ValueSource(strings = ["ru-ru", "en-us", "tr-tr"])
    fun `should success pass locale validation without Exceptions`(locale: String) {
        every { timeService.now() } returns LocalDateTime.now()
        every { wordsService.getVoicesForLocale(locale) } returns emptyList()
        // WHEN
        yandexSpeechKitService.validateLocaleAndVoice(locale, "")
    }

    @ParameterizedTest
    @ValueSource(strings = ["ruru", "en-en", "tr"])
    fun `should failed on locale validation`(locale: String) {
        // WHEN
        assertThrows<IllegalArgumentException> { yandexSpeechKitService.validateLocaleAndVoice(locale, "") }
    }

    @ParameterizedTest
    @ValueSource(strings = ["FILIPP", "NICK"])
    fun `should success pass voice validation without Exceptions`(voice: String) {
        val yandexVoices = listOf("FILIPP", "NICK")
        every { timeService.now() } returns LocalDateTime.now()
        every { wordsService.getVoicesForLocale("ru-ru") } returns yandexVoices
        // WHEN
        yandexSpeechKitService.validateLocaleAndVoice("ru-ru", voice)
    }

    @ParameterizedTest
    @ValueSource(strings = ["ddd", "rrr"])
    fun `should failed on voice validation`(voice: String) {
        val yandexVoices = listOf("FILIPP", "NICK")
        every { timeService.now() } returns LocalDateTime.now()
        every { wordsService.getVoicesForLocale("ru-ru") } returns yandexVoices
        // WHEN
        assertThrows<IllegalArgumentException> { yandexSpeechKitService.validateLocaleAndVoice("ru-ru", voice) }
    }

    @Test
    fun `should return current token in getYandexIamTokenForAudioGeneration`() {
        yandexSpeechKitService.iamToken = "current token"
        yandexSpeechKitService.iamTokenExpiresTime = LocalDateTime.now().plusHours(1)
        every { timeService.now() } returns LocalDateTime.now()
        // WHEN
        val resultToken = yandexSpeechKitService.getYandexIamTokenForAudioGeneration()
        // THEN
        resultToken shouldBe "current token"
    }

    @Test
    fun `should return new token in getYandexIamTokenForAudioGeneration`() {
        yandexSpeechKitService.iamToken = ""

        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()

        every { yandexIamTokenWebClient.post() } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(YandexIamTokenDto::class.java) } returns
            Mono.just(
                YandexIamTokenDto(
                    iamToken = "newIamToken",
                    expiresAt = "2040-11-24T11:48:38.503511+03:00",
                ),
            )
        every { timeService.now() } returns LocalDateTime.now()

        // WHEN
        val resultToken = yandexSpeechKitService.getYandexIamTokenForAudioGeneration()

        // THEN
        resultToken shouldBe "newIamToken"
    }

    @Test
    fun `should throw Exception if token request fails`() {
        yandexSpeechKitService.iamToken = ""

        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()

        every { yandexIamTokenWebClient.post() } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(YandexIamTokenDto::class.java) } returns Mono.empty()
        every { timeService.now() } returns LocalDateTime.now()

        // WHEN & THEN
        assertThrows<YandexServiceException> { yandexSpeechKitService.getYandexIamTokenForAudioGeneration() }
    }

    @Test
    fun `should generate audio stream from v3 response`() {
        val audioContent = "test audio content".toByteArray()
        val base64Audio = Base64.getEncoder().encodeToString(audioContent)

        yandexSpeechKitService.iamToken = "valid-token"
        yandexSpeechKitService.iamTokenExpiresTime = LocalDateTime.now().plusHours(1)
        every { timeService.now() } returns LocalDateTime.now()

        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()

        every { yandexTtsWebClient.post() } returns requestBodySpec
        every { requestBodySpec.header(any(), any()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToFlux(YandexTtsResponse::class.java) } returns
            Flux.just(
                YandexTtsResponse(
                    result =
                        YandexTtsResult(
                            audioChunk = AudioChunk(data = base64Audio),
                        ),
                ),
            )

        // WHEN
        val result =
            yandexSpeechKitService.generateAudioStream(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "filipp", speedFloat = "1.0"),
            )

        // THEN
        result.readBytes() shouldBe audioContent
    }

    @Test
    fun `should concatenate multiple audio chunks`() {
        val chunk1 = "chunk1".toByteArray()
        val chunk2 = "chunk2".toByteArray()
        val base64Chunk1 = Base64.getEncoder().encodeToString(chunk1)
        val base64Chunk2 = Base64.getEncoder().encodeToString(chunk2)

        yandexSpeechKitService.iamToken = "valid-token"
        yandexSpeechKitService.iamTokenExpiresTime = LocalDateTime.now().plusHours(1)
        every { timeService.now() } returns LocalDateTime.now()

        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()

        every { yandexTtsWebClient.post() } returns requestBodySpec
        every { requestBodySpec.header(any(), any()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToFlux(YandexTtsResponse::class.java) } returns
            Flux.just(
                YandexTtsResponse(result = YandexTtsResult(audioChunk = AudioChunk(data = base64Chunk1))),
                YandexTtsResponse(result = YandexTtsResult(audioChunk = AudioChunk(data = base64Chunk2))),
            )

        // WHEN
        val result =
            yandexSpeechKitService.generateAudioStream(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "filipp", speedFloat = "1.0"),
            )

        // THEN
        result.readBytes() shouldBe chunk1 + chunk2
    }

    @Test
    fun `should throw exception when audio response is empty`() {
        yandexSpeechKitService.iamToken = "valid-token"
        yandexSpeechKitService.iamTokenExpiresTime = LocalDateTime.now().plusHours(1)
        every { timeService.now() } returns LocalDateTime.now()

        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()

        every { yandexTtsWebClient.post() } returns requestBodySpec
        every { requestBodySpec.header(any(), any()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToFlux(YandexTtsResponse::class.java) } returns Flux.empty()

        // WHEN & THEN
        assertThrows<YandexServiceException> {
            yandexSpeechKitService.generateAudioStream(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "filipp", speedFloat = "1.0"),
            )
        }
    }
}
