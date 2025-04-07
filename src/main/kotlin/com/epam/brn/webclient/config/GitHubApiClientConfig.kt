package com.epam.brn.webclient.config

import com.epam.brn.webclient.customizer.WebClientLoggingCustomizer
import com.epam.brn.webclient.property.GitHubApiClientProperty
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class GitHubApiClientConfig(
    val property: GitHubApiClientProperty,
) {
    @Bean
    fun gitHubApiWebClient(webClientBuilder: WebClient.Builder): WebClient {
        val strategies =
            ExchangeStrategies
                .builder()
                .codecs { codecs: ClientCodecConfigurer ->
                    codecs.defaultCodecs().maxInMemorySize(property.codecMaxSize)
                }.build()

        if (property.loggingEnabled) {
            val customizer: WebClientCustomizer = WebClientLoggingCustomizer()
            customizer.customize(webClientBuilder)
        }

        val httpClient =
            HttpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, property.connectionTimeout)
                .doOnConnected { connection ->
                    connection.addHandlerLast(ReadTimeoutHandler(property.readTimeout))
                }

        val connector = ReactorClientHttpConnector(httpClient)
        return webClientBuilder
            .baseUrl(property.url.base)
            .clientConnector(connector)
            .exchangeStrategies(strategies)
            .build()
    }
}
