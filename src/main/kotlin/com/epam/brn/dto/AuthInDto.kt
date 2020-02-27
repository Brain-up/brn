package com.epam.brn.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude
data class AuthInDto(
    val grantType: String = "password",
    val password: String,
    val username: String,
    var audience: String? = null,
    var scope: String? = "read",
    var client_id: String? = null,
    var client_secret: String? = null
)
