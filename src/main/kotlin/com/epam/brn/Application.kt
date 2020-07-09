package com.epam.brn

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import springfox.documentation.swagger2.annotations.EnableSwagger2

@EnableSwagger2
@SpringBootApplication
@EnableScheduling
class Application

fun main(args: Array<String>) {
    // val mapper = ObjectMapper().registerModule(KotlinModule())
    SpringApplication.run(Application::class.java, *args)
}
