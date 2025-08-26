package com.epam.brn.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord
import org.springframework.stereotype.Service

@Service
class FirebaseUserService(
    private val firebaseAuth: FirebaseAuth,
) {
    fun getUserByUuid(uuid: String): UserRecord? = firebaseAuth.getUser(uuid)
}
