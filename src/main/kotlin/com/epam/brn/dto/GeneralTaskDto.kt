package com.epam.brn.dto

import com.epam.brn.model.ExerciseType

data class GeneralTaskDto(
    val id: Long,
    var level: Int? = 0,
    val exerciseType: ExerciseType,
    val name: String? = "",
    val serialNumber: Int? = 0,
    val answerOptions: Set<ResourceDto> = HashSet(),
)
