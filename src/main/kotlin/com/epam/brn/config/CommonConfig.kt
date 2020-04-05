package com.epam.brn.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommonConfig {
    @Bean("kotlinObjectMapper")
    fun kotlinObjectMapper(): ObjectMapper {
        return ObjectMapper().registerModule(KotlinModule())
    }
}
