package com.epam.brn.dto

import com.fasterxml.jackson.annotation.JsonIgnore

data class ExerciseDto(
    @JsonIgnore
    var seriesId: Long? = null,
    val id: Long?,
    val name: String,
    val description: String?,
    val level: Short? = 0,
    val tasks: MutableSet<TaskDto> = HashSet()
)