package com.epam.brn.dto

import javax.validation.constraints.NotBlank

data class ExerciseGroupDto(
    val id: Long?,
    @NotBlank
    var name: String?,
    var description: String?,
    val series: MutableSet<Long?> = HashSet()
) {
    constructor() : this(null, null, null)
}
