package com.epam.brn.config

import com.epam.brn.security.JwtTokenAuthenticationFilter
import com.epam.brn.security.JwtUsernameAndPasswordAuthenticationFilter
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class WebSecurityBasicConfiguration(
    @Qualifier("brainUpUserDetailService") private val userDetailsService: UserDetailsService,
    @Qualifier("kotlinObjectMapper") private val kotlinObjectMapper: ObjectMapper,
    @Qualifier("restAuthenticationEntryPoint") private val restAuthenticationEntryPoint: AuthenticationEntryPoint
) : WebSecurityConfigurerAdapter() {

    companion object {
        const val ADMIN = "ADMIN"
        const val USER = "USER"
    }

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder())
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf()
            .disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(restAuthenticationEntryPoint)
            .and()
            .addFilter(
                JwtUsernameAndPasswordAuthenticationFilter(
                    authenticationManager(),
                    jwtConfig(),
                    kotlinObjectMapper
                )
            )
            .addFilterAfter(JwtTokenAuthenticationFilter(jwtConfig()), UsernamePasswordAuthenticationFilter::class.java)
            .authorizeRequests()
            .antMatchers("/brnlogin").permitAll()
            .antMatchers("/registration").permitAll()
            .antMatchers("/admin/**").hasRole(ADMIN)
            .antMatchers("/users/current").hasAnyRole(ADMIN, USER)
            .antMatchers("/users/**").hasRole(ADMIN)
            .antMatchers("/cloud/upload").hasRole(ADMIN)
            .antMatchers("/cloud/folders").hasRole(ADMIN)
            .antMatchers("/**").hasAnyRole(ADMIN, USER)
            .anyRequest()
            .authenticated()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun brnAuthenticationManager(): AuthenticationManager = super.authenticationManagerBean()

    @Bean
    fun jwtConfig() = JwtConfig()
}
