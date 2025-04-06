package com.epam.brn.config

import com.epam.brn.auth.filter.FirebaseTokenAuthenticationFilter
import com.epam.brn.auth.filter.RememberLastVisitFilter
import com.epam.brn.enums.BrnRole
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import javax.servlet.http.HttpServletResponse

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
class WebSecurityBasicConfiguration(
    private val firebaseTokenAuthenticationFilter: FirebaseTokenAuthenticationFilter,
    private val rememberLastVisitFilter: RememberLastVisitFilter,
) : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .csrf()
            .disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(firebaseTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(rememberLastVisitFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeRequests()
            .antMatchers("/swagger-ui.html", "/swagger-resources/**", "/webjars/**")
            .hasRole(BrnRole.ADMIN)
            .and()
            .formLogin()
            .disable()
            .httpBasic()
            .disable()
            .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint())
            .accessDeniedHandler(accessDeniedHandler())
    }

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
    fun brnAuthenticationManager(): AuthenticationManager = super.authenticationManagerBean()
}
