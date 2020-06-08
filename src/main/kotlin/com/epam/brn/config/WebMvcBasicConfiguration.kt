package com.epam.brn.config

import java.util.Locale
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
}
