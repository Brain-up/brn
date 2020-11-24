package com.epam.brn.service

import com.epam.brn.dto.UserAccountDto

interface UserAccountService {
    fun findUserByName(name: String): UserAccountDto
    fun findUserByEmail(email: String): UserAccountDto
    fun addUser(userAccountDto: UserAccountDto): UserAccountDto
    fun save(userAccountDto: UserAccountDto): UserAccountDto
    fun findUserById(id: Long): UserAccountDto
    fun getUserFromTheCurrentSession(): UserAccountDto
    fun removeUserWithId(id: Long): Any
    fun getUsers(): List<UserAccountDto>
}
