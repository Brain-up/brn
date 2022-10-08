package com.epam.brn.webclient.config

import com.epam.brn.webclient.property.GitHubApiClientProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class GitHubApiClientConfig(
    val property: GitHubApiClientProperty,
    val webClientBuilder: WebClient.Builder
) {

    @Bean
    fun gitHubApiWebClient(): WebClient {
        return webClientBuilder
            .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
            .baseUrl(property.url.base)
            .build()
    }
}
