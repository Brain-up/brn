package com.epam.brn.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude
data class ExerciseRequest @JsonCreator constructor(
    @JsonProperty("ids")
    var ids: List<Long>
)
