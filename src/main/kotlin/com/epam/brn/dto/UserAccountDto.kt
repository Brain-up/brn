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
    val authorities: MutableSet<String>? = mutableSetOf(),
    val birthDate: LocalDate? = null
) {
    fun toModel() = UserAccount(
        id = id,
        userName = userName,
        email = email,
        password = password,
        active = active,
        birthDate = birthDate
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserAccountDto

        if (id != other.id) return false
        if (userName != other.userName) return false
        if (email != other.email) return false
        if (active != other.active) return false
        if (password != other.password) return false
        if (birthDate != other.birthDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + userName.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + active.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + (birthDate?.hashCode() ?: 0)
        return result
    }
}
