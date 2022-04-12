package com.epam.brn.service.azure.tts.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("azure.tts")
@ConditionalOnProperty(name = ["default.tts.provider"], havingValue = "azure")
class AzureTtsProperties {
    lateinit var ocpApimSubscriptionKey: String
    lateinit var baseUrl: String
    lateinit var allVoicesUrl: String
    lateinit var defaultVoiceName: String
    lateinit var defaultGender: String
    lateinit var defaultLang: String
    lateinit var defaultOutputFormat: String
    lateinit var acceptedLocales: List<String>
}
