package com.epam.brn.dto

import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoginDto(
    val grant_type: String = "password",
    @field:NotBlank
    @field:Email(message = "{group.validation.message.email}")
    val username: String,
    @field:NotBlank(message = "{group.validation.message.password}")
    var password: String
)
