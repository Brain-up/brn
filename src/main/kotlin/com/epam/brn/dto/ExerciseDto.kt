package com.epam.brn.dto

import com.fasterxml.jackson.annotation.JsonIgnore

data class ExerciseDto(
    var id: Long?,
    var name: String?,
    var description: String?,
    var level: Short? = 0,
    @JsonIgnore
    var seriesId: Long?,
    var available: Boolean? = null,
    var tasks: MutableSet<Long?> = HashSet()
)