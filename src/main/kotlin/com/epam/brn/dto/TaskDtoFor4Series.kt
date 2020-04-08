package com.epam.brn.dto

import com.epam.brn.model.ExerciseType
import com.fasterxml.jackson.annotation.JsonIgnore

data class TaskDtoFor4Series(
    val id: Long? = null,
    val exerciseType: ExerciseType = ExerciseType.SINGLE_SIMPLE_WORDS,
    @JsonIgnore
    val exerciseId: Long? = null,
    val name: String? = "",
    val serialNumber: Int? = 0,
    val answerOptions: Set<ResourceDto> = HashSet()
)
