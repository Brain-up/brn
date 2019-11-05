package com.epam.brn.dto

import javax.validation.constraints.NotBlank

data class SeriesDto(
    @NotBlank
    val exerciseGroupId: Long? = null,
    val id: Long?,
    @NotBlank
    val name: String,
    val description: String?,
    val exercises: MutableSet<Long?> = HashSet()
)