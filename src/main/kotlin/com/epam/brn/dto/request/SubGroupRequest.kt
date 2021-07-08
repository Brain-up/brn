package com.epam.brn.dto.request

import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import javax.validation.constraints.NotNull

class SubGroupRequest(
    @NotNull
    var name: String,
    @NotNull
    var level: Int,
    @NotNull
    var code: String,
    @NotNull
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
