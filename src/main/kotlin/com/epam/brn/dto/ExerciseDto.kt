package com.epam.brn.dto

import com.fasterxml.jackson.annotation.JsonIgnore

data class ExerciseDto(
    var id: Long?,
    var name: String?,
    var description: String?,
    var level: Short? = 0,
    @JsonIgnore
    var seriesId: Long? = null,
    var tasks: MutableSet<TaskDto>? = HashSet(),
    var available: Boolean? = null
) {
    constructor() : this(null, null, null, null, null, null)
}