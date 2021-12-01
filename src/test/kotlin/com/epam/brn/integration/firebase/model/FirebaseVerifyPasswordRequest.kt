package com.epam.brn.integration.firebase.model

data class FirebaseVerifyPasswordRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean
)
