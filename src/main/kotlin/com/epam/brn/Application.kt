package com.epam.brn

import com.epam.brn.webclient.property.GitHubApiClientProperty
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.EnableScheduling
import springfox.documentation.swagger2.annotations.EnableSwagger2

@EnableSwagger2
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(GitHubApiClientProperty::class)
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
