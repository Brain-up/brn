package com.epam.brn.auth

import com.epam.brn.dto.request.LoginDto
import com.epam.brn.dto.request.UserAccountRequest

interface AuthenticationService {
    fun login(loginDto: LoginDto): String
    fun registration(userAccountRequest: UserAccountRequest): String
}
