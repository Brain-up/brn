package com.epam.brn.dto

import com.epam.brn.enums.ExerciseType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SeriesDto(
    @field:NotNull
    val group: Long?,
    val id: Long?,
    val type: ExerciseType,
    @field:NotBlank
    val name: String,
    val level: Int,
    val description: String? = "",
    val active: Boolean = true,
    val subGroups: List<Long> = emptyList(),
)
