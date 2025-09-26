package com.epam.brn.config

import com.epam.brn.auth.filter.FirebaseTokenAuthenticationFilter
import com.epam.brn.auth.filter.RememberLastVisitFilter
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.BrainUpUserDetailsService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
class WebSecurityBasicConfiguration(
    private val firebaseTokenAuthenticationFilter: FirebaseTokenAuthenticationFilter,
    private val rememberLastVisitFilter: RememberLastVisitFilter,
    private val buUserDetailsService: BrainUpUserDetailsService,
) {
    @Bean
    @Throws(java.lang.Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain? =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(firebaseTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(rememberLastVisitFilter, UsernamePasswordAuthenticationFilter::class.java)
            .userDetailsService(buUserDetailsService)
            .authorizeHttpRequests {
                it.requestMatchers("/swagger-ui.html", "/swagger-resources/**", "/webjars/**").hasRole(BrnRole.ADMIN)
            }.formLogin { it.disable() }
            .httpBasic { it.disable() }
            .exceptionHandling { it.authenticationEntryPoint(authenticationEntryPoint()) }
            .build()

    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler? =
        AccessDeniedHandler { _, response: HttpServletResponse, _ ->
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied")
        }

    @Bean
    fun authenticationEntryPoint(): AuthenticationEntryPoint? =
        AuthenticationEntryPoint { _, response: HttpServletResponse, _ ->
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun brnAuthenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager
}
