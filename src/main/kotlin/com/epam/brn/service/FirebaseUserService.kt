package com.epam.brn.service

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
}
