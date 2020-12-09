package com.epam.brn.auth

import com.epam.brn.dto.request.LoginDto
import com.epam.brn.dto.request.UserAccountCreateRequest

interface AuthenticationService {
    fun login(loginDto: LoginDto): String
    fun registration(userAccountCreateRequest: UserAccountCreateRequest): String
}
