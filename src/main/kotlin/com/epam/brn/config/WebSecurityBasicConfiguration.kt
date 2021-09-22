package com.epam.brn.config

import com.epam.brn.enums.Role.ROLE_ADMIN
import com.epam.brn.enums.Role.ROLE_DOCTOR
import com.epam.brn.enums.Role.ROLE_USER
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
class WebSecurityBasicConfiguration(
    @Qualifier("brainUpUserDetailService") brainUpUserDetailService: UserDetailsService
) : WebSecurityConfigurerAdapter() {

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
            .authorizeRequests()
            .antMatchers("/brnlogin").permitAll()
            .antMatchers("/registration").permitAll()
            .antMatchers("/admin/**").hasAuthority(ROLE_ADMIN.name)
            .antMatchers("/users/current").hasAnyAuthority(ROLE_ADMIN.name, ROLE_USER.name)
            .antMatchers("/users/current/headphones").hasAnyAuthority(ROLE_ADMIN.name, ROLE_USER.name)
            .antMatchers("/users/current/*/doctor").hasAnyAuthority(ROLE_ADMIN.name, ROLE_USER.name)
            .antMatchers("/users/**").hasAuthority(ROLE_ADMIN.name)
            .antMatchers("/cloud/upload").hasAuthority(ROLE_ADMIN.name)
            .antMatchers("/cloud/folders").hasAuthority(ROLE_ADMIN.name)
            .antMatchers("/doctors/**").hasAnyAuthority(ROLE_ADMIN.name, ROLE_DOCTOR.name)
            .antMatchers("/**").hasAnyAuthority(ROLE_ADMIN.name, ROLE_USER.name)
            .and().formLogin()
            .and().httpBasic()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun brnAuthenticationManager(): AuthenticationManager = super.authenticationManagerBean()
}
