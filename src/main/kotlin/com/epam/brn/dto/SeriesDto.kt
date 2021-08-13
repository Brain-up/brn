package com.epam.brn.dto

import com.epam.brn.model.ExerciseType
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class SeriesDto(
    @field:NotNull
    val group: Long?,
    val id: Long?,
    val type: ExerciseType,
    @field:NotBlank
    val name: String,
    val level: Int,
    val description: String? = "",
    val subGroups: MutableSet<Long?> = HashSet()
)
