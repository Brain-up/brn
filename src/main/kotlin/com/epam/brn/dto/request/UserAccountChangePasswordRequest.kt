package com.epam.brn.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.validation.constraints.NotBlank

data class UserAccountChangePasswordRequest(
    @field:NotBlank(message = "{validation.field.password.blank}")
    val password: String = "",
    @field:NotBlank(message = "{validation.field.passwordConfirm.blank}")
    val passwordConfirm: String = ""
) {
    @field:JsonIgnore
    var uuid: String = ""
}
