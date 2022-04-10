package com.epam.brn.service.azure.tts.config

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
@ConditionalOnProperty(name = ["default.tts.provider"], havingValue = "azure")
class AzureTtsWebClientConfig(private val azureTtsProperties: AzureTtsProperties) {

    @Bean("azureTtsWebClient")
    fun azureTtsWebClient() = WebClient.builder()
        .baseUrl(azureTtsProperties.baseUrl)
        .clientConnector(reactorClientHttpConnector())
        .exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
                .build()
        )
        .build()

    @Bean("azureAllVoicesWebClient")
    fun azureAllVoicesWebClient() = WebClient.builder()
        .baseUrl(azureTtsProperties.allVoicesUrl)
        .clientConnector(reactorClientHttpConnector())
        .build()

    private fun reactorClientHttpConnector() = ReactorClientHttpConnector(
        HttpClient.create().responseTimeout(Duration.ofSeconds(10))
            .wiretap("reactor.netty.client.HttpClient", DEBUG, TEXTUAL)
    )
}
