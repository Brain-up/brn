package com.epam.brn.config

import com.epam.brn.auth.filter.FirebaseTokenAuthenticationFilter
import com.epam.brn.auth.filter.RememberLastVisitFilter
import com.epam.brn.enums.BrnRole
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import jakarta.servlet.http.HttpServletResponse

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true) // ✅ Обновлено!
class WebSecurityBasicConfiguration(
    private val firebaseTokenAuthenticationFilter: FirebaseTokenAuthenticationFilter,
    private val rememberLastVisitFilter: RememberLastVisitFilter,
    private val userDetailsService: UserDetailsService, // ✅ Добавьте, если используете DaoAuthenticationProvider
) {
    // ✅ Главный bean конфигурации безопасности (замена configure())
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // ✅ Новый DSL-синтаксис
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.addFilterBefore(firebaseTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(rememberLastVisitFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->
                // ✅ requestMatchers вместо antMatchers
                auth
                    .requestMatchers("/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/v3/api-docs/**")
                    .hasRole(BrnRole.ADMIN) // ✅ .name для enum Role
                    .requestMatchers("/public/**", "/auth/**") // ✅ Добавьте ваши публичные эндпоинты
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.formLogin { it.disable() }
            .httpBasic { it.disable() }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler())
            }
            // ✅ Если используете аутентификацию через UserDetailsService:
            .authenticationProvider(authenticationProvider())

        return http.build() // ✅ Обязательно!
    }

    // ✅ AuthenticationManager через AuthenticationConfiguration
    @Bean
    fun authenticationManager(
        config: org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration,
    ): AuthenticationManager = config.authenticationManager

    // ✅ AuthenticationProvider (если используете логин/пароль)
    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(userDetailsService)
        provider.setPasswordEncoder(passwordEncoder())
        return provider
    }

    // ✅ AccessDeniedHandler (обновлённый синтаксис)
    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler = AccessDeniedHandler { _, response, _ ->
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = "application/json"
        response.writer.write("""{"error": "Access Denied", "status": 403}""")
    }

    // ✅ AuthenticationEntryPoint (обновлённый синтаксис)
    @Bean
    fun authenticationEntryPoint(): AuthenticationEntryPoint = AuthenticationEntryPoint { _, response, _ ->
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.writer.write("""{"error": "Unauthorized", "status": 401}""")
    }

    // ✅ PasswordEncoder (без изменений)
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
