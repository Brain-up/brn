package com.epam.brn.service

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.dto.YandexIamTokenDto
import com.epam.brn.dto.yandex.tts.AudioChunk
import com.epam.brn.dto.yandex.tts.YandexTtsRequest
import com.epam.brn.dto.yandex.tts.YandexTtsResponse
import com.epam.brn.dto.yandex.tts.YandexTtsResult
import com.epam.brn.enums.Voice
import com.epam.brn.enums.VoiceRole
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

        every { yandexTtsProperties.folderId } returns "test-folder-id"
        every { yandexTtsProperties.authToken } returns "test-auth-token"
        every { yandexTtsProperties.preferredRole } returns "neutral"

        yandexSpeechKitService =
            YandexSpeechKitService(
                wordsService = wordsService,
                timeService = timeService,
                yandexTtsProperties = yandexTtsProperties,
                yandexTtsWebClient = yandexTtsWebClient,
                yandexIamTokenWebClient = yandexIamTokenWebClient,
            )
    }

    private fun mockIamTokenWebClient(mono: Mono<YandexIamTokenDto>): WebClient.ResponseSpec {
        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()

        every { yandexIamTokenWebClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(YandexIamTokenDto::class.java) } returns mono

        return responseSpec
    }

    private fun mockTtsWebClient(mono: Mono<String>): WebClient.RequestBodyUriSpec {
        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()

        every { yandexTtsWebClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.header(any(), any()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns mono

        return requestBodyUriSpec
    }

    private fun setValidToken() {
        yandexSpeechKitService.iamToken = "valid-token"
        yandexSpeechKitService.iamTokenExpiresTime = LocalDateTime.now().plusHours(1)
        every { timeService.now() } returns LocalDateTime.now()
    }

    private fun stubVoice(
        locale: String = "ru-ru",
        voiceName: String = Voice.FILIPP.name,
        voice: Voice = Voice.FILIPP,
    ) {
        every { wordsService.getVoicesForLocale(locale) } returns Voice.getVoicesForLocale(locale).map { it.name }
        every { wordsService.getVoiceForLocale(locale, voiceName) } returns voice
        every { wordsService.getVoiceForLocale(locale, voiceName.lowercase()) } returns voice
    }

    private fun buildNdjsonResponse(vararg chunks: String): String = chunks.joinToString("\n") { base64Data ->
        objectMapper.writeValueAsString(
            YandexTtsResponse(
                result = YandexTtsResult(audioChunk = AudioChunk(data = base64Data)),
            ),
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["ru-ru", "en-us"])
    fun `should pass locale validation for supported v3 locales`(locale: String) {
        every { wordsService.getVoicesForLocale(locale) } returns Voice.getVoicesForLocale(locale).map { it.name }

        yandexSpeechKitService.validateLocaleAndVoice(locale, "")
    }

    @ParameterizedTest
    @ValueSource(strings = ["ruru", "en-en", "tr-tr"])
    fun `should fail on locale validation when v3 has no voices for locale`(locale: String) {
        every { wordsService.getVoicesForLocale(locale) } returns emptyList()

        assertThrows<IllegalArgumentException> { yandexSpeechKitService.validateLocaleAndVoice(locale, "") }
    }

    @Test
    fun `should pass voice validation case-insensitively`() {
        every { wordsService.getVoicesForLocale("ru-ru") } returns Voice.getVoicesForLocale("ru-ru").map { it.name }
        every { wordsService.getVoiceForLocale("ru-ru", "filipp") } returns Voice.FILIPP

        yandexSpeechKitService.validateLocaleAndVoice("ru-ru", "filipp")
    }

    @Test
    fun `should fail on unsupported voice validation`() {
        every { wordsService.getVoicesForLocale("ru-ru") } returns Voice.getVoicesForLocale("ru-ru").map { it.name }
        every { wordsService.getVoiceForLocale("ru-ru", "ddd") } returns null

        assertThrows<IllegalArgumentException> { yandexSpeechKitService.validateLocaleAndVoice("ru-ru", "ddd") }
    }

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
        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()

        every { yandexIamTokenWebClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(capture(bodySlot)) } returns requestHeadersSpec
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
    fun `should throw exception if token request returns empty`() {
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
    fun `parseAudioChunks should skip malformed lines`() {
        val validAudio = "valid".toByteArray()
        val base64Valid = Base64.getEncoder().encodeToString(validAudio)
        val ndjson =
            listOf(
                "not-json",
                objectMapper.writeValueAsString(
                    YandexTtsResponse(
                        result = YandexTtsResult(audioChunk = AudioChunk(data = base64Valid)),
                    ),
                ),
            ).joinToString("\n")

        val chunks = yandexSpeechKitService.parseAudioChunks(ndjson)

        chunks.size shouldBe 1
        chunks[0] shouldBe validAudio
    }

    @Test
    fun `parseAudioChunks should return empty list for blank input`() {
        val chunks = yandexSpeechKitService.parseAudioChunks("")

        chunks.size shouldBe 0
    }

    @Test
    fun `should resolve preferred role when voice supports it`() {
        every { yandexTtsProperties.preferredRole } returns "neutral"

        val result = yandexSpeechKitService.resolvePreferredRole(Voice.FILIPP)

        result shouldBe VoiceRole.NEUTRAL
    }

    @Test
    fun `should skip preferred role when voice does not support it`() {
        every { yandexTtsProperties.preferredRole } returns "friendly"

        val result = yandexSpeechKitService.resolvePreferredRole(Voice.FILIPP)

        result shouldBe null
    }

    @Test
    fun `should generate audio stream with correct headers`() {
        val audioContent = "test audio content".toByteArray()
        val base64Audio = Base64.getEncoder().encodeToString(audioContent)
        val ndjson = buildNdjsonResponse(base64Audio)
        setValidToken()
        stubVoice()
        val requestBodyUriSpec = mockTtsWebClient(Mono.just(ndjson))

        val result =
            yandexSpeechKitService.generateAudioStream(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "filipp", speedFloat = "1.0"),
            )

        result.readBytes() shouldBe audioContent
        verify { requestBodyUriSpec.header("Authorization", "Bearer valid-token") }
        verify { requestBodyUriSpec.header("x-folder-id", "test-folder-id") }
    }

    @Test
    fun `should build correct TTS request with safe role hints`() {
        val audioContent = "audio".toByteArray()
        val ndjson = buildNdjsonResponse(Base64.getEncoder().encodeToString(audioContent))
        setValidToken()
        stubVoice()

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val bodySlot = slot<YandexTtsRequest>()

        every { yandexTtsWebClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.header(any(), any()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(capture(bodySlot)) } returns requestHeadersSpec
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
        captured.hints[2].role shouldBe "neutral"
    }

    @Test
    fun `should omit unsupported configured role from TTS request`() {
        val audioContent = "audio".toByteArray()
        val ndjson = buildNdjsonResponse(Base64.getEncoder().encodeToString(audioContent))
        setValidToken()
        every { yandexTtsProperties.preferredRole } returns "friendly"
        stubVoice()

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val bodySlot = slot<YandexTtsRequest>()

        every { yandexTtsWebClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.header(any(), any()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(capture(bodySlot)) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(ndjson)

        yandexSpeechKitService.generateAudioStream(
            AudioFileMetaData(text = "hello", locale = "ru-ru", voice = "FILIPP", speedFloat = "0.8"),
        )

        bodySlot.captured.hints.size shouldBe 2
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
        stubVoice()
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
        stubVoice()
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
        stubVoice()
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
        stubVoice()
        mockTtsWebClient(Mono.error(RuntimeException("Connection refused")))

        assertThrows<RuntimeException> {
            yandexSpeechKitService.generateAudioStream(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "filipp", speedFloat = "1.0"),
            )
        }
    }

    @Test
    fun `should generate audio with explicit voice`() {
        val audioContent = "audio".toByteArray()
        val ndjson = buildNdjsonResponse(Base64.getEncoder().encodeToString(audioContent))
        setValidToken()
        stubVoice()
        mockTtsWebClient(Mono.just(ndjson))

        val result =
            yandexSpeechKitService.generateAudioOggStreamWithValidation(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "filipp", speedFloat = "1.0"),
            )

        result.readBytes() shouldBe audioContent
    }

    @Test
    fun `should fall back to first male voice when voice is empty`() {
        val audioContent = "audio".toByteArray()
        val ndjson = buildNdjsonResponse(Base64.getEncoder().encodeToString(audioContent))
        setValidToken()
        every { wordsService.getVoicesForLocale("ru-ru") } returns Voice.getVoicesForLocale("ru-ru").map { it.name }
        every { wordsService.getDefaultVoiceForLocale("ru-ru") } returns Voice.FILIPP.name
        every { wordsService.getVoiceForLocale("ru-ru", Voice.FILIPP.name) } returns Voice.FILIPP
        mockTtsWebClient(Mono.just(ndjson))

        val result =
            yandexSpeechKitService.generateAudioOggStreamWithValidation(
                AudioFileMetaData(text = "test", locale = "ru-ru", voice = "", speedFloat = "1.0"),
            )

        result.readBytes() shouldBe audioContent
        verify { wordsService.getDefaultVoiceForLocale("ru-ru") }
    }

    @Test
    fun `should throw on invalid locale in generateAudioOggStreamWithValidation`() {
        every { wordsService.getVoicesForLocale("tr-tr") } returns emptyList()

        assertThrows<IllegalArgumentException> {
            yandexSpeechKitService.generateAudioOggStreamWithValidation(
                AudioFileMetaData(text = "test", locale = "tr-tr", voice = "", speedFloat = "1.0"),
            )
        }
    }
}
