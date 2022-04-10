package com.epam.brn.wiremock

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@ActiveProfiles("integration-tests")
@TestPropertySource(properties = ["default.tts.provider=azure"])
class WiremockTestConfiguration {

    @Value("\${azure.tts.base-url}")
    private lateinit var baseUrl: String

    @Value("\${azure.tts.all-voices-url}")
    private lateinit var allVoicesUrl: String

    @Bean("azureTtsWebClient")
    fun azureTtsWebClient(builder: WebClient.Builder) = builder.baseUrl(baseUrl).build()

    @Bean("azureAllVoicesWebClient")
    fun azureAllVoicesWebClient(builder: WebClient.Builder) = builder.baseUrl(allVoicesUrl).build()
}
