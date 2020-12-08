package com.epam.brn.service

import com.epam.brn.dto.request.UserAccountRequest
import com.epam.brn.dto.response.UserAccountDto

interface UserAccountService {
    fun findUserByName(name: String): UserAccountDto
    fun findUserByEmail(email: String): UserAccountDto
    fun addUser(userAccountRequest: UserAccountRequest): UserAccountDto
    fun save(userAccountRequest: UserAccountRequest): UserAccountDto
    fun findUserById(id: Long): UserAccountDto
    fun getUserFromTheCurrentSession(): UserAccountDto
    fun removeUserWithId(id: Long): Any
    fun getUsers(): List<UserAccountDto>
    fun updateAvatarCurrentUser(avatarUrl: String): UserAccountDto
}
