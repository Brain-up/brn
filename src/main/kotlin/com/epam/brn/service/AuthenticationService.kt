package com.epam.brn.service

import com.epam.brn.dto.LoginDto
import com.epam.brn.dto.UserAccountDto
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

interface AuthenticationService {
    fun login(loginDto: LoginDto): UsernamePasswordAuthenticationToken
    fun registration(userAccountDto: UserAccountDto): UsernamePasswordAuthenticationToken
}
