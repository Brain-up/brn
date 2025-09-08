package com.epam.brn.dto

import com.epam.brn.enums.ExerciseType
import java.lang.Boolean.TRUE
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
    val active: Boolean = TRUE,
    val subGroups: List<Long> = emptyList(),
)
