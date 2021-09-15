package com.epam.brn.dto

import javax.validation.constraints.NotBlank

data class ExerciseGroupDto(
    val id: Long?,
    @field:NotBlank
    var locale: String,
    @field:NotBlank
    var name: String,
    var description: String?,
    val series: MutableList<Long?> = mutableListOf()
)
