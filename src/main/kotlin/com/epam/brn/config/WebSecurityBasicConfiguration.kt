package com.epam.brn.config

import com.epam.brn.dto.JwtConfig
import com.epam.brn.security.JwtTokenAuthenticationFilter
import com.epam.brn.security.JwtUsernameAndPasswordAuthenticationFilter
import javax.servlet.http.HttpServletResponse
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class WebSecurityBasicConfiguration(
    @Qualifier("brainUpUserDetailService") brainUpUserDetailService: UserDetailsService
) : WebSecurityConfigurerAdapter() {

    companion object {
        const val ADMIN = "ADMIN"
        const val USER = "USER"
    }

    private val userDetailsService: UserDetailsService = brainUpUserDetailService

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
            .authenticationEntryPoint { _, response, _ -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED) }
            .and()
            .addFilter(JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(), jwtConfig()))
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
