package com.epam.brn.service.yandex.tts.config

import io.netty.handler.logging.LogLevel.DEBUG
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL
import java.time.Duration

@Configuration
@Profile("!integration-tests")
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

    private fun reactorClientHttpConnector() = ReactorClientHttpConnector(
        HttpClient
            .create()
            .responseTimeout(Duration.ofSeconds(15))
            .wiretap("reactor.netty.client.HttpClient", DEBUG, TEXTUAL),
    )
}
