package com.epam.brn.service

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.dto.YandexIamTokenDto
import com.epam.brn.dto.yandex.tts.AudioChunk
import com.epam.brn.dto.yandex.tts.YandexTtsRequest
import com.epam.brn.dto.yandex.tts.YandexTtsResponse
import com.epam.brn.dto.yandex.tts.YandexTtsResult
import com.epam.brn.exception.YandexServiceException
import com.epam.brn.service.yandex.tts.config.YandexTtsProperties
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Base64

internal class YandexSpeechKitServiceTest {
    private lateinit var yandexSpeechKitService: YandexSpeechKitService
    private lateinit var wordsService: WordsService
    private lateinit var timeService: TimeService
    private lateinit var yandexTtsProperties: YandexTtsProperties
    private lateinit var yandexTtsWebClient: WebClient
    private lateinit var yandexIamTokenWebClient: WebClient
    private val objectMapper = ObjectMapper()

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

    // region Helper methods

    private fun mockIamTokenWebClient(mono: Mono<YandexIamTokenDto>): WebClient.ResponseSpec {
        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()

        every { yandexIamTokenWebClient.post() } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(YandexIamTokenDto::class.java) } returns mono
        return responseSpec
    }

    private fun mockTtsWebClient(mono: Mono<String>): WebClient.RequestBodySpec {
        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()

        every { yandexTtsWebClient.post() } returns requestBodySpec
        every { requestBodySpec.header(any(), any()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns mono
        return requestBodySpec
    }

    private fun setValidToken() {
        yandexSpeechKitService.iamToken = "valid-token"
        yandexSpeechKitService.iamTokenExpiresTime = LocalDateTime.now().plusHours(1)
        every { timeService.now() } returns LocalDateTime.now()
    }

    private fun buildNdjsonResponse(vararg chunks: String): String = chunks.joinToString("\n") { base64Data ->
        objectMapper.writeValueAsString(
            YandexTtsResponse(
                result = YandexTtsResult(audioChunk = AudioChunk(data = base64Data)),
            ),
        )
    }

    // endregion

    // region Locale and voice validation

    @ParameterizedTest
    @ValueSource(strings = ["ru-ru", "en-us", "tr-tr"])
    fun `should success pass locale validation without Exceptions`(locale: String) {
        every { wordsService.getVoicesForLocale(locale) } returns emptyList()
        yandexSpeechKitService.validateLocaleAndVoice(locale, "")
    }

    @ParameterizedTest
    @ValueSource(strings = ["ruru", "en-en", "tr"])
    fun `should failed on locale validation`(locale: String) {
        assertThrows<IllegalArgumentException> { yandexSpeechKitService.validateLocaleAndVoice(locale, "") }
    }

    @ParameterizedTest
    @ValueSource(strings = ["FILIPP", "NICK"])
    fun `should success pass voice validation without Exceptions`(voice: String) {
        val yandexVoices = listOf("FILIPP", "NICK")
        every { wordsService.getVoicesForLocale("ru-ru") } returns yandexVoices
        yandexSpeechKitService.validateLocaleAndVoice("ru-ru", voice)
    }

    @ParameterizedTest
    @ValueSource(strings = ["ddd", "rrr"])
    fun `should failed on voice validation`(voice: String) {
        val yandexVoices = listOf("FILIPP", "NICK")
        every { wordsService.getVoicesForLocale("ru-ru") } returns yandexVoices
        assertThrows<IllegalArgumentException> { yandexSpeechKitService.validateLocaleAndVoice("ru-ru", voice) }
    }

    // endregion

    // region IAM token

    @Test
    fun `should return current token in getYandexIamTokenForAudioGeneration`() {
        yandexSpeechKitService.iamToken = "current token"
        yandexSpeechKitService.iamTokenExpiresTime = LocalDateTime.now().plusHours(1)
        every { timeService.now() } returns LocalDateTime.now()

        val resultToken = yandexSpeechKitService.getYandexIamTokenForAudioGeneration()

        resultToken shouldBe "current token"
    }

    @Test
    fun `should return new token and parse expiry correctly`() {
        yandexSpeechKitService.iamToken = ""
        every { timeService.now() } returns LocalDateTime.now()

        val bodySlot = slot<Map<String, String>>()
        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()

        every { yandexIamTokenWebClient.post() } returns requestBodySpec
        every { requestBodySpec.bodyValue(capture(bodySlot)) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(YandexIamTokenDto::class.java) } returns
            Mono.just(
                YandexIamTokenDto(
                    iamToken = "newIamToken",
                    expiresAt = "2040-11-24T11:48:38.503511+03:00",
                ),
            )

        val resultToken = yandexSpeechKitService.getYandexIamTokenForAudioGeneration()

        resultToken shouldBe "newIamToken"
        bodySlot.captured["yandexPassportOauthToken"] shouldBe "test-auth-token"
        val expectedExpiry =
            ZonedDateTime
                .parse("2040-11-24T11:48:38.503511+03:00")
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime()
        yandexSpeechKitService.iamTokenExpiresTime shouldBe expectedExpiry
    }

    @Test
    fun `should throw Exception if token request returns empty`() {
        yandexSpeechKitService.iamToken = ""
        every { timeService.now() } returns LocalDateTime.now()

        mockIamTokenWebClient(Mono.empty())

        assertThrows<YandexServiceException> { yandexSpeechKitService.getYandexIamTokenForAudioGeneration() }
    }

    @Test
    fun `should propagate exception when token request fails with error`() {
        yandexSpeechKitService.iamToken = ""
        every { timeService.now() } returns LocalDateTime.now()

        mockIamTokenWebClient(Mono.error(RuntimeException("Connection refused")))

        assertThrows<RuntimeException> { yandexSpeechKitService.getYandexIamTokenForAudioGeneration() }
    }

    // endregion

    // region parseAudioChunks

    @Test
    fun `parseAudioChunks should decode base64 audio from NDJSON`() {
        val audioContent = "test audio content".toByteArray()
        val base64Audio = Base64.getEncoder().encodeToString(audioContent)
        val ndjson = buildNdjsonResponse(base64Audio)

        val chunks = yandexSpeechKitService.parseAudioChunks(ndjson)

        chunks.size shouldBe 1
        chunks[0] shouldBe audioContent
    }

    @Test
    fun `parseAudioChunks should concatenate multiple chunks`() {
        val chunk1 = "chunk1".toByteArray()
        val chunk2 = "chunk2".toByteArray()
        val ndjson =
            buildNdjsonResponse(
                Base64.getEncoder().encodeToString(chunk1),
                Base64.getEncoder().encodeToString(chunk2),
            )

        val chunks = yandexSpeechKitService.parseAudioChunks(ndjson)

        chunks.size shouldBe 2
        chunks[0] shouldBe chunk1
        chunks[1] shouldBe chunk2
    }

    @Test
    fun `parseAudioChunks should skip responses with null result or audioChunk`() {
        val validAudio = "valid".toByteArray()
        val base64Valid = Base64.getEncoder().encodeToString(validAudio)
        val ndjsonLines =
            listOf(
                objectMapper.writeValueAsString(YandexTtsResponse(result = null)),
                objectMapper.writeValueAsString(
                    YandexTtsResponse(result = YandexTtsResult(audioChunk = AudioChunk(data = base64Valid))),
                ),
                objectMapper.writeValueAsString(YandexTtsResponse(result = YandexTtsResult(audioChunk = null))),
            )
        val ndjson = ndjsonLines.joinToString("\n")

        val chunks = yandexSpeechKitService.parseAudioChunks(ndjson)

        chunks.size shouldBe 1
        chunks[0] shouldBe validAudio
    }

    @Test
    fun `parseAudioChunks should return empty list for blank input`() {
        val chunks = yandexSpeechKitService.parseAudioChunks("")
        chunks.size shouldBe 0
    }

    // endregion

    // region generateAudioStream

    @Test
    fun `should generate audio stream with correct headers and body`() {
        val audioContent = "test audio content".toByteArray()
        val base64Audio = Base64.getEncoder().encodeToString(audioContent)
        val ndjson = buildNdjsonResponse(base64Audio)
        setValidToken()

        val requestBodySpec = mockTtsWebClient(Mono.just(ndjson))

        val result =
            yandexSpeechKitService.generateAudioStream(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "filipp", speedFloat = "1.0"),
            )

        result.readBytes() shouldBe audioContent
        verify { requestBodySpec.header("Authorization", "Bearer valid-token") }
        verify { requestBodySpec.header("x-folder-id", "test-folder-id") }
    }

    @Test
    fun `should build correct TTS request with hints`() {
        val audioContent = "audio".toByteArray()
        val ndjson = buildNdjsonResponse(Base64.getEncoder().encodeToString(audioContent))
        setValidToken()

        val requestBodySpec = mockk<WebClient.RequestBodySpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val bodySlot = slot<YandexTtsRequest>()

        every { yandexTtsWebClient.post() } returns requestBodySpec
        every { requestBodySpec.header(any(), any()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(capture(bodySlot)) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(ndjson)

        yandexSpeechKitService.generateAudioStream(
            AudioFileMetaData(text = "hello", locale = "ru-ru", voice = "FILIPP", speedFloat = "0.8"),
        )

        val captured = bodySlot.captured
        captured.text shouldBe "hello"
        captured.outputAudioSpec.containerAudio.containerAudioType shouldBe "OGG_OPUS"
        captured.hints.size shouldBe 3
        captured.hints[0].voice shouldBe "filipp"
        captured.hints[1].speed shouldBe "0.8"
        captured.hints[2].role shouldBe "friendly"
    }

    @Test
    fun `should concatenate multiple audio chunks from NDJSON response`() {
        val chunk1 = "chunk1".toByteArray()
        val chunk2 = "chunk2".toByteArray()
        val ndjson =
            buildNdjsonResponse(
                Base64.getEncoder().encodeToString(chunk1),
                Base64.getEncoder().encodeToString(chunk2),
            )
        setValidToken()
        mockTtsWebClient(Mono.just(ndjson))

        val result =
            yandexSpeechKitService.generateAudioStream(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "filipp", speedFloat = "1.0"),
            )

        result.readBytes() shouldBe chunk1 + chunk2
    }

    @Test
    fun `should throw exception when audio response is empty`() {
        setValidToken()
        mockTtsWebClient(Mono.just(""))

        assertThrows<YandexServiceException> {
            yandexSpeechKitService.generateAudioStream(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "filipp", speedFloat = "1.0"),
            )
        }
    }

    @Test
    fun `should throw exception when WebClient returns null`() {
        setValidToken()
        mockTtsWebClient(Mono.empty())

        assertThrows<YandexServiceException> {
            yandexSpeechKitService.generateAudioStream(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "filipp", speedFloat = "1.0"),
            )
        }
    }

    @Test
    fun `should propagate exception when TTS request fails`() {
        setValidToken()
        mockTtsWebClient(Mono.error(RuntimeException("Connection refused")))

        assertThrows<RuntimeException> {
            yandexSpeechKitService.generateAudioStream(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "filipp", speedFloat = "1.0"),
            )
        }
    }

    // endregion

    // region generateAudioOggStreamWithValidation

    @Test
    fun `should generate audio with explicit voice`() {
        val audioContent = "audio".toByteArray()
        val ndjson = buildNdjsonResponse(Base64.getEncoder().encodeToString(audioContent))
        setValidToken()
        mockTtsWebClient(Mono.just(ndjson))
        every { wordsService.getVoicesForLocale("ru-ru") } returns listOf("filipp")

        val result =
            yandexSpeechKitService.generateAudioOggStreamWithValidation(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "filipp", speedFloat = "1.0"),
            )

        result.readBytes() shouldBe audioContent
    }

    @Test
    fun `should fall back to default woman voice when voice is empty`() {
        val audioContent = "audio".toByteArray()
        val ndjson = buildNdjsonResponse(Base64.getEncoder().encodeToString(audioContent))
        setValidToken()
        mockTtsWebClient(Mono.just(ndjson))
        every { wordsService.getVoicesForLocale("ru-ru") } returns emptyList()
        every { wordsService.getDefaultWomanVoiceForLocale("ru-ru") } returns "oksana"

        val result =
            yandexSpeechKitService.generateAudioOggStreamWithValidation(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "", speedFloat = "1.0"),
            )

        result.readBytes() shouldBe audioContent
        verify { wordsService.getDefaultWomanVoiceForLocale("ru-ru") }
    }

    @Test
    fun `should throw on invalid locale in generateAudioOggStreamWithValidation`() {
        assertThrows<IllegalArgumentException> {
            yandexSpeechKitService.generateAudioOggStreamWithValidation(
                AudioFileMetaData(text = "test", locale = "invalid", voice = "", speedFloat = "1.0"),
            )
        }
    }

    // endregion
}
