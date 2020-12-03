package com.epam.brn.service

import com.epam.brn.dto.request.UserAccountRequest
import com.epam.brn.dto.response.UserAccountResponse

interface UserAccountService {
    fun findUserByName(name: String): UserAccountResponse
    fun findUserByEmail(email: String): UserAccountResponse
    fun addUser(userAccountRequest: UserAccountRequest): UserAccountResponse
    fun save(userAccountRequest: UserAccountRequest): UserAccountResponse
    fun findUserById(id: Long): UserAccountResponse
    fun getUserFromTheCurrentSession(): UserAccountResponse
    fun removeUserWithId(id: Long): Any
    fun getUsers(): List<UserAccountResponse>
    fun updateAvatarCurrentUser(url: String): UserAccountResponse
}
