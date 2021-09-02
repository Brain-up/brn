package com.epam.brn.service

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountAdditionalInfoRequest
import com.epam.brn.dto.request.UserAccountChangePasswordRequest
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.model.UserAccount
import com.google.firebase.auth.UserRecord
import org.springframework.data.domain.Pageable

interface UserAccountService {
    fun findUserByName(name: String): UserAccountResponse
    fun findUserByEmail(email: String): UserAccountResponse
    fun createUser(
        userAccountCreateRequest: UserAccountCreateRequest,
        firebaseUserRecord: UserRecord
    ): UserAccountResponse

    fun findUserById(id: Long): UserAccountResponse
    fun getUserFromTheCurrentSession(): UserAccountResponse
    fun getUsers(pageable: Pageable): List<UserAccountResponse>
    fun getUsersWithAnalytics(pageable: Pageable): List<UserWithAnalyticsResponse>
    fun updateAvatarForCurrentUser(avatarUrl: String): UserAccountResponse
    fun updateCurrentUser(userChangeRequest: UserAccountChangeRequest): UserAccountResponse
    fun addHeadphonesToUser(userId: Long, headphonesDto: HeadphonesDto): HeadphonesDto
    fun addHeadphonesToCurrentUser(headphones: HeadphonesDto): HeadphonesDto
    fun getCurrentUser(): UserAccount
    fun findUserEntityById(id: Long): UserAccount
    fun getAllHeadphonesForUser(userId: Long): Set<HeadphonesDto>
    fun getAllHeadphonesForCurrentUser(): Set<HeadphonesDto>
    fun findUserByUuid(uuid: String): UserAccountResponse?
    fun createUserWithFirebase(
        additionalInfoRequest: UserAccountAdditionalInfoRequest,
        firebaseUserRecord: UserRecord
    ): UserAccountResponse

    fun changePasswordCurrentUser(
        userAccountChangePasswordRequest: UserAccountChangePasswordRequest
    ): Boolean
}
