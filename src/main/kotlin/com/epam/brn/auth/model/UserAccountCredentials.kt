package com.epam.brn.auth.model

import com.google.firebase.auth.FirebaseToken

data class UserAccountCredentials(
    private val decodedToken: FirebaseToken?,
    private val idToken: String?
)
