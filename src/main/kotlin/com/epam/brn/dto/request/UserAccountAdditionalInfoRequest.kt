package com.epam.brn.dto.request

import com.epam.brn.model.Gender
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserAccountAdditionalInfoRequest(
    @field:NotNull(message = "{validation.field.bornYear.notNull}")
    var bornYear: Int = 1,
    @field:NotNull(message = "{validation.field.gender.notNull}")
    val gender: Gender,
    val avatar: String? = null
) {
    @field:JsonIgnore
    var uuid: String = ""
}
