package com.epam.brn.service

import com.epam.brn.dto.HeadphonesDto
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
    fun addHeadphonesToUser(userId: Long, headphonesDto: HeadphonesDto): HeadphonesDto
    fun addHeadphonesToCurrentUser(headphones: HeadphonesDto): HeadphonesDto
    fun getCurrentUser(): UserAccount
    fun findUserEntityById(id: Long): UserAccount
    fun getAllHeadphonesForUser(userId: Long): Set<HeadphonesDto>
    fun getAllHeadphonesForCurrentUser(): Set<HeadphonesDto>
    fun getAllUsersByAuthorityName(name: String): List<UserAccountDto>
}
