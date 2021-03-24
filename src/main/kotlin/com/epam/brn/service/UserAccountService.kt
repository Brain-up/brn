package com.epam.brn.service

import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.dto.response.UserWithAnalyticsDto
import com.epam.brn.model.UserAccount
import org.springframework.data.domain.Pageable

interface UserAccountService {
    fun findUserByName(name: String): UserAccountDto
    fun findUserByEmail(email: String): UserAccountDto
    fun addUser(userAccountCreateRequest: UserAccountCreateRequest): UserAccountDto
    fun save(userAccountCreateRequest: UserAccountCreateRequest): UserAccountDto
    fun findUserById(id: Long): UserAccountDto
    fun getUserFromTheCurrentSession(): UserAccountDto
    fun getUsers(pageable: Pageable): List<UserAccountDto>
    fun getUsersWithAnalytics(pageable: Pageable): List<UserWithAnalyticsDto>
    fun updateAvatarForCurrentUser(avatarUrl: String): UserAccountDto
    fun updateCurrentUser(userChangeRequest: UserAccountChangeRequest): UserAccountDto

    fun getCurrentUser(): UserAccount
}
