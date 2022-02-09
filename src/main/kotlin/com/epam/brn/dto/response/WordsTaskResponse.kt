package com.epam.brn.dto.response

import com.epam.brn.dto.ResourceDto
import com.epam.brn.model.ExerciseType
import com.fasterxml.jackson.annotation.JsonIgnore

data class WordsTaskResponse(
    val id: Long,
    val exerciseType: ExerciseType,
    @JsonIgnore
    val exerciseId: Long? = null,
    val name: String? = "",
    val serialNumber: Int? = 0,
    val answerOptions: Set<ResourceDto> = HashSet()
)
