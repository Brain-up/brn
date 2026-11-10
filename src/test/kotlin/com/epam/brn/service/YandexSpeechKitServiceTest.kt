package com.epam.brn.service

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.exception.YandexServiceException
import com.epam.brn.service.monitoring.AlertService
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.kotest.assertions.throwables.shouldThrow
import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.InputStream

@ExtendWith(MockKExtension::class)
internal class YandexSpeechKitServiceTest {
    @InjectMockKs
    lateinit var yandexSpeechKitService: YandexSpeechKitService

    @MockK
    lateinit var wordsService: WordsService

    @MockK
    lateinit var alertService: AlertService

    @ParameterizedTest
    @ValueSource(strings = ["ru-ru", "en-us", "tr-tr"])
    fun `should success pass locale validation without Exceptions`(locale: String) {
        every { wordsService.getVoicesForLocale(locale) } returns emptyList()
        // WHEN
        yandexSpeechKitService.validateLocaleAndVoice(locale, "")
    }

    @ParameterizedTest
    @ValueSource(strings = ["ruru", "en-en", "tr"])
    fun `should failed on locale validation`(locale: String) {
        // WHEN
        shouldThrow<IllegalArgumentException> { yandexSpeechKitService.validateLocaleAndVoice(locale, "") }
    }

    @ParameterizedTest
    @ValueSource(strings = ["FILIPP", "NICK"])
    fun `should success pass voice validation without Exceptions`(voice: String) {
        val yandexVoices = listOf("FILIPP", "NICK")
        every { wordsService.getVoicesForLocale("ru-ru") } returns yandexVoices
        // WHEN
        yandexSpeechKitService.validateLocaleAndVoice("ru-ru", voice)
    }

    @ParameterizedTest
    @ValueSource(strings = ["ddd", "rrr"])
    fun `should failed on voice validation`(voice: String) {
        val yandexVoices = listOf("FILIPP", "NICK")
        every { wordsService.getVoicesForLocale("ru-ru") } returns yandexVoices
        // WHEN
        shouldThrow<IllegalArgumentException> { yandexSpeechKitService.validateLocaleAndVoice("ru-ru", voice) }
    }

    @Test
    fun `should authenticate audio generation request with Api-Key header`() {
        // GIVEN
        yandexSpeechKitService.apiKey = "apiKeyValue"
        yandexSpeechKitService.uriGenerationAudioFile = "http://yandex/tts"
        yandexSpeechKitService.folderId = "folderId"
        yandexSpeechKitService.format = "oggopus"
        yandexSpeechKitService.emotions = listOf("friendly")

        val httpClientBuilder = mockk<HttpClientBuilder>()
        val httpClient = mockk<CloseableHttpClient>()
        val httpResponse = mockk<CloseableHttpResponse>()
        val httpEntity = mockk<HttpEntity>()
        val inputStream = mockk<InputStream>()
        val requestSlot = slot<HttpPost>()

        mockkStatic(HttpClientBuilder::class)
        every { HttpClientBuilder.create() } returns httpClientBuilder
        every { httpClientBuilder.build() } returns httpClient
        every { httpClient.execute(capture(requestSlot)) } returns httpResponse
        every { httpResponse.statusLine.statusCode } returns 200
        every { httpResponse.entity } returns httpEntity
        every { httpEntity.content } returns inputStream

        // WHEN
        val result = yandexSpeechKitService.generateAudioStream(AudioFileMetaData("text", "ru-ru", "oksana", "1"))

        // THEN
        result shouldBe inputStream
        requestSlot.captured.getFirstHeader("Authorization").value shouldBe "Api-Key apiKeyValue"
        requestSlot.captured.uri.toString() shouldContain "http://yandex/tts"

        unmockkStatic(HttpClientBuilder::class)
    }

    @Test
    fun `should send an alert and throw when yandex returns a non-200 status`() {
        // GIVEN
        yandexSpeechKitService.apiKey = "apiKeyValue"
        yandexSpeechKitService.uriGenerationAudioFile = "http://yandex/tts"
        yandexSpeechKitService.folderId = "folderId"
        yandexSpeechKitService.format = "oggopus"
        yandexSpeechKitService.emotions = listOf("friendly")

        val httpClientBuilder = mockk<HttpClientBuilder>()
        val httpClient = mockk<CloseableHttpClient>()
        val httpResponse = mockk<CloseableHttpResponse>()
        val httpEntity = mockk<HttpEntity>()
        val inputStream = mockk<InputStream>()

        mockkStatic(HttpClientBuilder::class)
        every { HttpClientBuilder.create() } returns httpClientBuilder
        every { httpClientBuilder.build() } returns httpClient
        every { httpClient.execute(any<HttpPost>()) } returns httpResponse
        every { httpResponse.statusLine.statusCode } returns 401
        every { httpResponse.entity } returns httpEntity
        every { httpEntity.content } returns inputStream
        every { alertService.sendAlert(any(), any()) } just Runs

        // WHEN
        shouldThrow<YandexServiceException> {
            yandexSpeechKitService.generateAudioStream(AudioFileMetaData("text", "ru-ru", "oksana", "1"))
        }

        // THEN
        verify { alertService.sendAlert("yandex-tts", any()) }

        unmockkStatic(HttpClientBuilder::class)
    }
}
