package com.epam.brn.dto.response

import com.epam.brn.enums.ExerciseType

data class GeneralTaskResponse(
    val id: Long,
    var level: Int? = 0,
    val exerciseType: ExerciseType,
    val name: String? = "",
    val serialNumber: Int? = 0,
    val answerOptions: Set<ResourceResponse> = HashSet(),
)
