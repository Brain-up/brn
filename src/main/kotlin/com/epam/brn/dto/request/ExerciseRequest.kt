package com.epam.brn.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude
data class ExerciseRequest @JsonCreator constructor(
    @param:JsonProperty("ids")
    @get:JsonProperty("ids")
    var ids: List<Long>
)
