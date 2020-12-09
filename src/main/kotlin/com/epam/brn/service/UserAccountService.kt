package com.epam.brn.service

import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountDto

interface UserAccountService {
    fun findUserByName(name: String): UserAccountDto
    fun findUserByEmail(email: String): UserAccountDto
    fun addUser(userAccountCreateRequest: UserAccountCreateRequest): UserAccountDto
    fun save(userAccountCreateRequest: UserAccountCreateRequest): UserAccountDto
    fun findUserById(id: Long): UserAccountDto
    fun getUserFromTheCurrentSession(): UserAccountDto
    fun getUsers(): List<UserAccountDto>
    fun updateAvatarForCurrentUser(avatarUrl: String): UserAccountDto
    fun updateCurrentUser(userChangeRequest: UserAccountChangeRequest): UserAccountDto
}
