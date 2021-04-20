package com.epam.brn.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.validator.constraints.Length

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateResourceDescriptionRequest(
    @field:Length(min = 1, max = 255)
    val description: String? = null
)
