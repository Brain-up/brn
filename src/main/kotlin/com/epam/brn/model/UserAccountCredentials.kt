package com.epam.brn.model

import com.google.firebase.auth.FirebaseToken

data class UserAccountCredentials(
    private val decodedToken: FirebaseToken?,
    private val idToken: String?
)
