package com.epam.brn.dto

import com.epam.brn.model.UserAccount
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

const val VALID_EMAIL_ADDRESS_REGEX_WITH_EMPTY_SPACES_ACCEPTANCE: String =
    "(^\\s+$)|([a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)"

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserAccountDto(
    val id: Long? = null,
    @field:NotBlank(message = "{validation.field.first-name.no-spaces}")
    val firstName: String,
    @field:NotBlank(message = "{validation.field.last-name.no-spaces}")
    val lastName: String,
    @field:NotBlank(message = "{validation.field.email.blank}")
    @field:Email(message = "{validation.field.email.invalid-format}")
    @field:Pattern(
        regexp = VALID_EMAIL_ADDRESS_REGEX_WITH_EMPTY_SPACES_ACCEPTANCE,
        message = "{validation.field.email.invalid-format.cyrillic.not.allowed}"
    )
    val email: String,
    @field:NotBlank(message = "{validation.field.password.blank}")
    @field:Size(min = 4, max = 20, message = "{validation.field.password.invalid-format}")
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
