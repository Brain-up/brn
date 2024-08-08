package com.epam.brn.service

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.model.UserAccount
import com.google.firebase.auth.UserRecord
import org.springframework.data.domain.Pageable

interface UserAccountService {
    fun findUserByEmail(email: String): UserAccountDto
    fun createUser(
        firebaseUserRecord: UserRecord
    ): UserAccountDto

    fun getCurrentUser(): UserAccount
    fun findUserById(id: Long): UserAccount
    fun getCurrentUserId(): Long
    fun getCurrentUserRoles(): Set<String>
    fun getCurrentUserDto(): UserAccountDto
    fun findUserDtoById(id: Long): UserAccountDto
    fun findUserDtoByUuid(uuid: String): UserAccountDto?

    fun getUsers(pageable: Pageable, role: String): List<UserAccountDto>
    fun updateAvatarForCurrentUser(avatarUrl: String): UserAccountDto
    fun updateCurrentUser(userChangeRequest: UserAccountChangeRequest): UserAccountDto

    fun addHeadphonesToUser(userId: Long, headphonesDto: HeadphonesDto): HeadphonesDto
    fun addHeadphonesToCurrentUser(headphones: HeadphonesDto): HeadphonesDto
    fun deleteHeadphonesForCurrentUser(headphonesId: Long)
    fun getAllHeadphonesForUser(userId: Long): Set<HeadphonesDto>
    fun getAllHeadphonesForCurrentUser(): Set<HeadphonesDto>

    fun updateDoctorForPatient(userId: Long, doctorId: Long): UserAccount
    fun removeDoctorFromPatient(userId: Long): UserAccount
    fun getPatientsForDoctor(doctorId: Long): List<UserAccountDto>

    fun markVisitForCurrentUser()
    fun deleteAutoTestUsers(): Long
    fun deleteAutoTestUserByEmail(email: String): Long
}
