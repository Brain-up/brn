package com.epam.brn.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class LoginDto(
    val grant_type: String = "password",
    @field:NotBlank(message = "{validation.field.email.blank}")
    @field:Email(message = "{validation.field.email.invalid-format}")
    val username: String,
    @field:NotBlank(message = "{validation.field.password.blank}")
    var password: String
)
