package com.epam.brn.integration.configuration

import com.epam.brn.config.AwsConfig
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Properties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AwsTestConfig {
    @Bean
    fun awsConfig(): AwsConfig {
        return object : AwsConfig() {
            override fun instant(): OffsetDateTime = Instant.ofEpochMilli(1580384357114).atOffset(ZoneOffset.UTC)
            override fun uuid(): String = "c49791b2-b27b-4edf-bac8-8734164c20e6"
            override fun initCredentials(): Properties {
                val properties = Properties()
                properties.setProperty("aws.accessKeyId", "AKIAI7KLKATWVCMEKGPA")
                properties.setProperty("aws.secretAccessKey", "99999999999999999999999999999")
                return properties
            }
        }
    }
}
