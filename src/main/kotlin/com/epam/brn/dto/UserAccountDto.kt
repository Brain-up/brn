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
    @field:NotBlank(message = "{first.name.without.spaces}")
    val firstName: String,
    @field:NotBlank(message = "{last.name.without.spaces}")
    val lastName: String,
    @field:NotBlank
    @field:Email
    val email: String,
    @field:NotBlank
    @field:Size(min = 4)
    var password: String,
    val birthday: LocalDate? = null,
    var active: Boolean = true
) {
    var authorities: MutableSet<String>? = mutableSetOf()
    fun toModel(hashedPassword: String) = UserAccount(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        password = hashedPassword,
        birthday = birthday,
        active = active
    )
}
