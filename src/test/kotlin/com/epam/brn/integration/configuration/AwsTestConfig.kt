package com.epam.brn.integration.configuration

import com.epam.brn.config.AwsConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Configuration
class AwsTestConfig {
    @Bean
    fun awsConfig(
        @Value("\${cloud.expireAfterDuration}") expireAfterDuration: String,
        @Value("\${aws.accessRuleCanned}") accessRuleCanned: String,
        @Value("\${aws.credentialsPath}") credentialsPath: String,
        @Value("\${aws.accessKeyId}") accessKeyIdProperty: String,
        @Value("\${aws.secretAccessKey}") secretAccessKeyProperty: String,
        @Value("\${aws.region}") region: String
    ): AwsConfig {
        return object : AwsConfig(expireAfterDuration, accessRuleCanned, credentialsPath, accessKeyIdProperty, secretAccessKeyProperty, region) {
            override fun instant(): OffsetDateTime = Instant.ofEpochMilli(1580384357114).atOffset(ZoneOffset.UTC)
            override fun uuid(): String = "c49791b2-b27b-4edf-bac8-8734164c20e6"
        }
    }
}
