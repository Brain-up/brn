package com.epam.brn.service

import com.epam.brn.dto.request.UserAccountCreateRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord
import org.springframework.stereotype.Service

@Service
class FirebaseUserService(
    private val firebaseAuth: FirebaseAuth
) {

    fun getUserById(uuid: String): UserRecord? {
        return firebaseAuth.getUser(uuid)
    }

    fun getUserByEmail(email: String): UserRecord? {
        return firebaseAuth.getUserByEmail(email)
    }

    fun addUser(userAccountCreateRequest: UserAccountCreateRequest): UserRecord? {
        val firebaseUser = UserRecord.CreateRequest()
            .setEmail(userAccountCreateRequest.email)
            .setDisplayName(userAccountCreateRequest.name)
            .setPassword(userAccountCreateRequest.password)
            .setEmailVerified(false)
        if (userAccountCreateRequest.avatar != null) {
            firebaseUser
                .setPhotoUrl(userAccountCreateRequest.avatar)
        }
        val createdUser = firebaseAuth.createUser(firebaseUser)
        return createdUser
    }

    fun deleteUser(uuid: String) {
        firebaseAuth.deleteUser(uuid)
    }
}
