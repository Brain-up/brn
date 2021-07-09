package com.epam.brn.dto.request

import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import javax.validation.constraints.NotBlank

data class SubGroupRequest(
    @NotBlank
    var name: String,
    @NotBlank
    var level: Int,
    @NotBlank
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
