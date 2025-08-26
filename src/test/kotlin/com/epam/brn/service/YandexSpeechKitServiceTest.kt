package com.epam.brn.service

import com.epam.brn.exception.YandexServiceException
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.InputStream
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
internal class YandexSpeechKitServiceTest {
    @InjectMockKs
    lateinit var yandexSpeechKitService: YandexSpeechKitService

    @MockK
    lateinit var wordsService: WordsService

    @MockK
    lateinit var timeService: TimeService

    @ParameterizedTest
    @ValueSource(strings = ["ru-ru", "en-us", "tr-tr"])
    fun `should success pass locale validation without Exceptions`(locale: String) {
        every { timeService.now() } returns LocalDateTime.now()
        every { wordsService.getVoicesForLocale(locale) } returns emptyList()
        // WHENv
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
        yandexSpeechKitService.authToken = "authToken"
        yandexSpeechKitService.uriGetIamToken = "uriGetIamToken"

        val httpClientBuilder = mockk<HttpClientBuilder>()
        val httpClient = mockk<CloseableHttpClient>()
        val httpResponse = mockk<CloseableHttpResponse>()
        val httpEntity = mockk<HttpEntity>()
        val inputStream = mockk<InputStream>()
        mockkStatic(HttpClientBuilder::class)
        every { HttpClientBuilder.create() } returns httpClientBuilder
        mockkStatic(EntityUtils::class)
        every { EntityUtils.toString(any()) } returns "{\n" +
            " \"iamToken\": \"iamTokenValue\",\n" +
            " \"expiresAt\": \"2040-11-24T11:48:38.503511+03:00\"\n" +
            "}"

        every { httpClientBuilder.build() } returns httpClient
        every { httpClient.execute(any()) } returns httpResponse
        every { httpResponse.statusLine.statusCode } returns 200
        every { httpResponse.entity } returns httpEntity
        every { httpEntity.content } returns inputStream
        every { timeService.now() } returns LocalDateTime.now()
        // WHEN
        val resultToken = yandexSpeechKitService.getYandexIamTokenForAudioGeneration()
        // THEN
        resultToken shouldBe "iamTokenValue"
        httpResponse.statusLine.statusCode shouldBe 200

        unmockkStatic(HttpClientBuilder::class)
        unmockkStatic(EntityUtils::class)
    }

    @Test
    fun `should throw Exception if status code is not 200`() {
        yandexSpeechKitService.iamToken = ""
        yandexSpeechKitService.authToken = "authToken"
        yandexSpeechKitService.uriGetIamToken = "uriGetIamToken"

        val httpClientBuilder = mockk<HttpClientBuilder>()
        val httpClient = mockk<CloseableHttpClient>()
        val httpResponse = mockk<CloseableHttpResponse>()

        mockkStatic(HttpClientBuilder::class)
        every { HttpClientBuilder.create() } returns httpClientBuilder
        every { httpClientBuilder.build() } returns httpClient
        every { httpClient.execute(any()) } returns httpResponse
        every { httpResponse.statusLine.statusCode } returns 100
        // WHEN & THEN
        assertThrows<YandexServiceException> { yandexSpeechKitService.getYandexIamTokenForAudioGeneration() }
    }
}
