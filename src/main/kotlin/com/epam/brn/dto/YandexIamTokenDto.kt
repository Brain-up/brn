package com.epam.brn.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude
data class YandexIamTokenDto(
    val iamToken: String,
    val expiresAt: String
)
