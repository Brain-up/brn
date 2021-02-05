package com.epam.brn.dto

import com.epam.brn.enums.ExerciseType
import javax.validation.constraints.NotBlank

data class SeriesDto(
    @NotBlank
    val group: Long? = null,
    val id: Long?,
    @NotBlank
    val type: ExerciseType,
    @NotBlank
    val name: String,
    val level: Int,
    val description: String? = "",
    val subGroups: MutableSet<Long?> = HashSet()
)
