package com.epam.brn.service.impl

import com.auth0.jwt.JWT
import com.epam.brn.dto.AuthInDto
import com.epam.brn.dto.LoginDto
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.service.AuthenticationService
import com.epam.brn.service.UserAccountService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthenticationServiceImpl(private val userAccountService: UserAccountService) : AuthenticationService {
    private val log = logger()

    override fun registration(userAccountDto: UserAccountDto): UsernamePasswordAuthenticationToken {
        userAccountService.addUser(userAccountDto)
        val token = UsernamePasswordAuthenticationToken(userAccountDto.email, userAccountDto.password)
        return token
    }

    override fun login(loginDto: LoginDto): UsernamePasswordAuthenticationToken {
        val token = UsernamePasswordAuthenticationToken(loginDto.username, loginDto.password)
        return token
    }

    fun createToken(authInDto: AuthInDto): String {
        val jwt = JWT.create()
        return jwt.toString()
    }
}
