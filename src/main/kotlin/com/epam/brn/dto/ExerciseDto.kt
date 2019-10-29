package com.epam.brn.dto

import com.fasterxml.jackson.annotation.JsonIgnore

class ExerciseDto(
    val id: Long?,
    val name: String,
    val description: String?,
    val level: Short? = 0,
    @JsonIgnore
    var seriesId: Long? = null,
    val tasks: MutableSet<TaskDto> = HashSet()
)