package com.epam.brn.auth

import com.epam.brn.dto.LoginDto
import com.epam.brn.dto.UserAccountDto

interface AuthenticationService {
    fun login(loginDto: LoginDto): String
    fun registration(userAccountDto: UserAccountDto): String
}
