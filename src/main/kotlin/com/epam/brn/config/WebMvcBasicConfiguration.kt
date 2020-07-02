package com.epam.brn.config

import java.util.Locale
import javax.validation.Validator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver

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
    fun validator(): Validator? {
        val factory = LocalValidatorFactoryBean()
        factory.setValidationMessageSource(messageSource()!!)
        return factory
    }

    @Bean
    fun messageSource(): ReloadableResourceBundleMessageSource? {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasenames(
            "classpath:/messages",
            "classpath:authErrorResponse"
        )
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }
}
