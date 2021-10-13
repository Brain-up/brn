package com.epam.brn.config

import com.epam.brn.auth.filter.FirebaseTokenAuthenticationFilter
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
class WebSecurityBasicConfiguration(
    private val firebaseTokenAuthenticationFilter: FirebaseTokenAuthenticationFilter
) : WebSecurityConfigurerAdapter() {

    companion object {
        const val ADMIN = "ADMIN"
        const val USER = "USER"
    }

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf()
            .disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(firebaseTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeRequests()
            .antMatchers("/registration/**").permitAll()
            .antMatchers("/admin/**").hasRole(ADMIN)
            .antMatchers("/users/current").hasAnyRole(ADMIN, USER)
            .antMatchers("/users/current/headphones").hasAnyRole(ADMIN, USER)
            .antMatchers("/users/current/password").hasAnyRole(ADMIN, USER)
            .antMatchers("/users/**").hasRole(ADMIN)
            .antMatchers("/cloud/upload").hasRole(ADMIN)
            .antMatchers("/cloud/folders").hasRole(ADMIN)
            .antMatchers("/**").hasAnyRole(ADMIN, USER)
            .and().formLogin().disable()
            .httpBasic().disable()
            .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint())
            .accessDeniedHandler(accessDeniedHandler())
    }

    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler? {
        return AccessDeniedHandler { request: HttpServletRequest, response: HttpServletResponse, e: AccessDeniedException ->
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied")
        }
    }

    @Bean
    fun authenticationEntryPoint(): AuthenticationEntryPoint? {
        return AuthenticationEntryPoint { request: HttpServletRequest, response: HttpServletResponse, e: AuthenticationException ->
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun brnAuthenticationManager(): AuthenticationManager = super.authenticationManagerBean()
}
