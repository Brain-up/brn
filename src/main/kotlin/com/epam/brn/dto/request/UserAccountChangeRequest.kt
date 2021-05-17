package com.epam.brn.dto.request

import com.epam.brn.model.Gender
import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Min

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserAccountChangeRequest(
    val name: String? = null,
    @field:Min(value = 1900, message = "{validation.field.bornYear.moreThen1900}")
    val bornYear: Int? = null,
    val gender: Gender? = null,
    val avatar: String? = null,
    val photo: String? = null,
    @field:Length(min = 1, max = 255)
    val description: String? = null
)
