package com.epam.brn

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import springfox.documentation.swagger2.annotations.EnableSwagger2

@EnableSwagger2
@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}