package com.epam.brn.dto

import javax.validation.constraints.NotBlank

data class ExerciseGroupDto(
    val id: Long?,
    @NotBlank
    val name: String?,
    val description: String?,
    val series: MutableSet<SeriesDto> = HashSet()
) {
    constructor() : this(null, null, null)
}
