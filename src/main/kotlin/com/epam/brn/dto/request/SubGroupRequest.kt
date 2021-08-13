package com.epam.brn.dto.request

import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.fasterxml.jackson.annotation.JsonCreator
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class SubGroupRequest @JsonCreator internal constructor (
    @field:NotBlank
    var name: String,
    @field:NotNull
    var level: Int?,
    @field:NotBlank
    var code: String,
    @field:NotBlank
    var description: String?
) {
    fun toModel(series: Series) = SubGroup(
        name = this.name,
        level = this.level!!,
        code = this.code,
        description = this.description,
        series = series
    )
}
