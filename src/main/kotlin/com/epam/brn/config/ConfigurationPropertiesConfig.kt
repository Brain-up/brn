package com.epam.brn.config

import com.epam.brn.webclient.property.GitHubApiClientProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(GitHubApiClientProperty::class)
class ConfigurationPropertiesConfig
