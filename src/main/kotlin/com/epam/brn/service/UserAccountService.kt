package com.epam.brn.service

import com.epam.brn.model.UserAccount

interface UserAccountService {
    fun findUserByName(name: String): UserAccount
}