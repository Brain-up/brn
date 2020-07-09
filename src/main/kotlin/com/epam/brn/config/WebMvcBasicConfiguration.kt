package com.epam.brn.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.nio.charset.StandardCharsets
import java.util.Locale

@Configuration
class WebMvcBasicConfiguration : WebMvcConfigurer {

    @Bean
    fun localeResolver(): LocaleResolver {
        val slr = SessionLocaleResolver()
        slr.setDefaultLocale(Locale("ru", "RU"))
        return slr
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(LocaleChangeInterceptor())
    }

    @Bean
    override fun getValidator(): LocalValidatorFactoryBean {
        val validatorBean = LocalValidatorFactoryBean()
        validatorBean.setValidationMessageSource(messageSource())
        return validatorBean
    }

    @Bean
    fun messageSource(): ReloadableResourceBundleMessageSource {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasenames(
            "classpath:/messages",
            "classpath:errorMessages"
        )
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name())
        return messageSource
    }

    @Bean
    fun objectMapperBuilder(): Jackson2ObjectMapperBuilder =
        Jackson2ObjectMapperBuilder().modulesToInstall(KotlinModule())
}
