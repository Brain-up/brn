package com.epam.brn.service.yandex.tts.config

import io.netty.handler.logging.LogLevel.DEBUG
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL
import java.time.Duration

@Configuration
@ConditionalOnProperty(name = ["default.tts.provider"], havingValue = "yandex")
class YandexTtsWebClientConfig(
    private val yandexTtsProperties: YandexTtsProperties,
) {
    @Bean("yandexTtsWebClient")
    fun yandexTtsWebClient() = WebClient
        .builder()
        .baseUrl(yandexTtsProperties.generationAudioLink)
        .clientConnector(reactorClientHttpConnector())
        .exchangeStrategies(
            ExchangeStrategies
                .builder()
                .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
                .build(),
        ).build()

    @Bean("yandexIamTokenWebClient")
    fun yandexIamTokenWebClient() = WebClient
        .builder()
        .baseUrl(yandexTtsProperties.getTokenLink)
        .clientConnector(reactorClientHttpConnector())
        .build()

    private fun reactorClientHttpConnector() = ReactorClientHttpConnector(httpClient())

    private fun httpClient(): HttpClient {
        val client = HttpClient.create().responseTimeout(Duration.ofSeconds(15))

        return if (yandexTtsProperties.enableWiretap)
            client.wiretap("reactor.netty.client.HttpClient", DEBUG, TEXTUAL)
        else
            client
    }
}
