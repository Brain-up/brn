package com.epam.brn.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfig {
    val basePackage = "com.epam.brn.controller"

    @Bean
    fun toolingApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .groupName("all")
            .apiInfo(apiInfo())
            .select()
            // following line will expose only internal/service APIs
            // if Actuator endpoints have to be exposed as well - comment it out
            .apis(RequestHandlerSelectors.basePackage(basePackage))
            .paths(PathSelectors.any())
            .build()
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfoBuilder()
            .title("Brain up project")
            .description("REST API for brn")
            .contact(Contact("Elena.Moshnikova", "https://www.epam.com/", "elena_moshnikova@epam.com"))
            .build()
    }

    @Override
    protected fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/")
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/")
    }
}
