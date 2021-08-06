package com.epam.brn.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoginDto(
    val grant_type: String = "password",
    @field:NotBlank(message = "{validation.field.email.blank}")
    @field:Email(message = "{validation.field.email.invalid-format}")
    val username: String,
    @field:NotBlank(message = "{validation.field.password.blank}")
    var password: String
)
