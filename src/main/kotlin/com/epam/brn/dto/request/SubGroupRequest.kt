package com.epam.brn.dto.request

import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class SubGroupRequest(
    @field: NotBlank
    var name: String,
    @field:NotNull
    var level: Int,
    @field: NotBlank
    var code: String,
    var description: String?
) {
    fun toModel(series: Series) = SubGroup(
        name = this.name,
        level = this.level,
        code = this.code,
        description = this.description,
        series = series
    )
}
