package com.epam.brn.service

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.model.UserAccount
import com.google.firebase.auth.UserRecord
import org.springframework.data.domain.Pageable

interface UserAccountService {
    fun findUserByEmail(email: String): UserAccountResponse
    fun createUser(
        firebaseUserRecord: UserRecord
    ): UserAccountResponse

    fun findUserById(id: Long): UserAccountResponse
    fun getUserFromTheCurrentSession(): UserAccountResponse
    fun getUsers(pageable: Pageable, role: String): List<UserAccountResponse>
    fun updateAvatarForCurrentUser(avatarUrl: String): UserAccountResponse
    fun updateCurrentUser(userChangeRequest: UserAccountChangeRequest): UserAccountResponse
    fun addHeadphonesToUser(userId: Long, headphonesDto: HeadphonesDto): HeadphonesDto
    fun addHeadphonesToCurrentUser(headphones: HeadphonesDto): HeadphonesDto
    fun deleteHeadphonesForCurrentUser(headphonesId: Long)
    fun getCurrentUser(): UserAccount
    fun getCurrentUserId(): Long
    fun findUserEntityById(id: Long): UserAccount
    fun getAllHeadphonesForUser(userId: Long): Set<HeadphonesDto>
    fun getAllHeadphonesForCurrentUser(): Set<HeadphonesDto>
    fun findUserByUuid(uuid: String): UserAccountResponse?
    fun updateDoctorForPatient(userId: Long, doctorId: Long): UserAccount
    fun removeDoctorFromPatient(userId: Long): UserAccount
    fun getPatientsForDoctor(doctorId: Long): List<UserAccountResponse>
}
