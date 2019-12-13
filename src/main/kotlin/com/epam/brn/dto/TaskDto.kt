package com.epam.brn.dto

import com.fasterxml.jackson.annotation.JsonIgnore

data class TaskDto(
    val id: Long? = null,
    @JsonIgnore
    val exerciseId: Long? = null,
    val name: String? = "",
    val correctAnswer: ResourceDto? = null,
    val serialNumber: Int? = 0,
    val answerOptions: MutableSet<ResourceDto> = HashSet()
)