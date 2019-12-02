package com.epam.brn.dto

import java.time.LocalDate

data class UserAccountDto(
    val id: Long? = null,
    val userName: String,
    val email: String,
    val active: Boolean,
    val birthDate: LocalDate? = null
) {}