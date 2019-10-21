package com.epam.brn.dto

import javax.validation.constraints.NotBlank

data class SeriesDto(
    val id: Long?,
    @NotBlank
    val name: String,
    val description: String?,
    @NotBlank
    val exerciseGroup: Long? = null,
    val exercises: MutableSet<ExerciseDto> = HashSet()
)