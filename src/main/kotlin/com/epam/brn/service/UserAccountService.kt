package com.epam.brn.service

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.model.UserAccount
import org.springframework.data.domain.Pageable

interface UserAccountService {
    fun findUserByName(name: String): UserAccountResponse
    fun findUserByEmail(email: String): UserAccountResponse
    fun addUser(userAccountCreateRequest: UserAccountCreateRequest): UserAccountResponse
    fun save(userAccountCreateRequest: UserAccountCreateRequest): UserAccountResponse
    fun findUserById(id: Long): UserAccountResponse
    fun getUserFromTheCurrentSession(): UserAccountResponse
    fun getUsers(pageable: Pageable, role: String): List<UserAccountResponse>
    fun getUsersWithAnalytics(pageable: Pageable, role: String): List<UserWithAnalyticsResponse>
    fun updateAvatarForCurrentUser(avatarUrl: String): UserAccountResponse
    fun updateCurrentUser(userChangeRequest: UserAccountChangeRequest): UserAccountResponse
    fun addHeadphonesToUser(userId: Long, headphonesDto: HeadphonesDto): HeadphonesDto
    fun addHeadphonesToCurrentUser(headphones: HeadphonesDto): HeadphonesDto
    fun deleteHeadphonesForCurrentUser(id: Long)
    fun getCurrentUser(): UserAccount
    fun findUserEntityById(id: Long): UserAccount
    fun getAllHeadphonesForUser(userId: Long): Set<HeadphonesDto>
    fun getAllHeadphonesForCurrentUser(): Set<HeadphonesDto>
}
