package com.epam.brn.dto

import com.epam.brn.model.UserAccount
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserAccountDto(
    val id: Long? = null,
    @field:NotBlank
    val userName: String,
    @field:NotBlank
    @field:Email
    val email: String,
    val active: Boolean = true,
    @field:NotBlank
    @field:Size(min = 4)
    var password: String? = null,
    val birthDate: LocalDate? = null
) {
    var authorities: MutableSet<String>? = mutableSetOf()
    fun toModel() = UserAccount(
        id = id,
        userName = userName,
        email = email,
        password = password,
        active = active,
        birthDate = birthDate
    )
}
