package com.epam.brn.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude
data class AuthOutResponse(
    val access_token: String,
    val token_type: String = "Bearer",
    val expires_in: Long = 36000
)
