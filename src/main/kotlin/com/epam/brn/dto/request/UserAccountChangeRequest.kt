package com.epam.brn.dto.request

import com.epam.brn.enums.BrnGender
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.Min

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserAccountChangeRequest(
    val name: String? = null,
    @field:Min(value = 1900, message = "{validation.field.bornYear.moreThen1900}")
    val bornYear: Int? = null,
    val gender: BrnGender? = null,
    val avatar: String? = null,
    val photo: String? = null,
    val description: String? = null
)
