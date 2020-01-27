package com.epam.brn.service

import com.epam.brn.dto.UserAccountDto

interface UserAccountService {
    fun findUserByName(name: String): UserAccountDto
}
