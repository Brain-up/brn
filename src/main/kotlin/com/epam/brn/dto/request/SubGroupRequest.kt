package com.epam.brn.dto.request

import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SubGroupRequest @JsonCreator internal constructor (
    @get:JsonProperty(value = "first-property", required = true)
    @param:JsonProperty(value = "first-property", required = true)
    var name: String,
    @get:JsonProperty(value = "second-property", required = true)
    @param:JsonProperty(value = "second-property", required = true)
    var level: Int,
    @get:JsonProperty("third-property", required = true)
    @param:JsonProperty("third-property", required = true)
    var code: String,
    @get:JsonProperty("fourth-property")
    @param:JsonProperty("fourth-property")
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
