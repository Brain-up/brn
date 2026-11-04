package com.epam.brn.service.monitoring

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

internal class TelegramAlertServiceTest {
    private lateinit var service: TelegramAlertService

    private val httpClientBuilder = mockk<HttpClientBuilder>()
    private val httpClient = mockk<CloseableHttpClient>(relaxed = true)
    private val httpResponse = mockk<CloseableHttpResponse>(relaxed = true)

    @BeforeEach
    fun setUp() {
        service =
            TelegramAlertService().apply {
                enabled = true
                botToken = "botToken"
                chatId = "chatId"
                apiUrl = "https://api.telegram.org"
                minInterval = Duration.ofMinutes(10)
            }
        mockkStatic(HttpClientBuilder::class)
        every { HttpClientBuilder.create() } returns httpClientBuilder
        every { httpClientBuilder.build() } returns httpClient
        every { httpClient.execute(any<HttpPost>()) } returns httpResponse
        every { httpResponse.statusLine.statusCode } returns 200
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(HttpClientBuilder::class)
    }

    @Test
    fun `should POST the alert to the telegram bot api when enabled`() {
        // GIVEN
        val requestSlot = slot<HttpPost>()
        every { httpClient.execute(capture(requestSlot)) } returns httpResponse

        // WHEN
        service.sendAlert("yandex-tts", "something broke")

        // THEN
        verify { httpClient.execute(any<HttpPost>()) }
        requestSlot.captured.uri.toString() shouldBe "https://api.telegram.org/botbotToken/sendMessage"
    }

    @Test
    fun `should not send anything when disabled`() {
        // GIVEN
        service.enabled = false

        // WHEN
        service.sendAlert("yandex-tts", "something broke")

        // THEN
        verify(exactly = 0) { httpClient.execute(any<HttpPost>()) }
    }

    @Test
    fun `should not send anything when not configured`() {
        // GIVEN
        service.botToken = ""

        // WHEN
        service.sendAlert("yandex-tts", "something broke")

        // THEN
        verify(exactly = 0) { httpClient.execute(any<HttpPost>()) }
    }

    @Test
    fun `should suppress a repeated alert for the same key within the cooldown`() {
        // WHEN
        service.sendAlert("yandex-tts", "something broke")
        service.sendAlert("yandex-tts", "something broke again")

        // THEN
        verify(exactly = 1) { httpClient.execute(any<HttpPost>()) }
    }

    @Test
    fun `should send alerts for different keys independently`() {
        // WHEN
        service.sendAlert("yandex-tts", "yandex broke")
        service.sendAlert("azure-tts", "azure broke")

        // THEN
        verify(exactly = 2) { httpClient.execute(any<HttpPost>()) }
    }

    @Test
    fun `should swallow delivery failures`() {
        // GIVEN
        every { httpClient.execute(any<HttpPost>()) } throws RuntimeException("network down")

        // WHEN / THEN (no exception propagates)
        service.sendAlert("yandex-tts", "something broke")
    }
}
