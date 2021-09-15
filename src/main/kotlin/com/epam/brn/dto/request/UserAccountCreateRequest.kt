package com.epam.brn.dto.request

import com.epam.brn.model.Gender
import com.epam.brn.model.UserAccount
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

const val VALID_EMAIL_ADDRESS_REGEX_WITH_EMPTY_SPACES_ACCEPTANCE: String =
    "(^\\s+$)|([a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)"

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserAccountCreateRequest(
    @field:NotBlank(message = "{validation.field.fullName.empty}")
    val name: String,
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
    @field:NotNull(message = "{validation.field.bornYear.notNull}")
    val bornYear: Int?,
    @field:NotNull(message = "{validation.field.gender.notNull}")
    val gender: Gender,
    val avatar: String? = null,
    val photo: String? = null,
    val description: String? = null
) {
    var authorities: MutableSet<String>? = mutableSetOf()
    fun toModel(hashedPassword: String) = UserAccount(
        fullName = name,
        email = email,
        password = hashedPassword,
        bornYear = bornYear,
        gender = gender.toString(),
        avatar = avatar,
        photo = photo,
        description = description
    )
}
