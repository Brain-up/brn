package com.epam.brn.dto.response

import com.epam.brn.enums.ExerciseMechanism
import com.epam.brn.enums.ExerciseType
import com.epam.brn.enums.toMechanism

data class TaskResponse(
    val id: Long,
    var level: Int? = 0,
    val exerciseType: ExerciseType,
    val exerciseMechanism: ExerciseMechanism = exerciseType.toMechanism(),
    val name: String? = "",
    val serialNumber: Int? = 0,
    val answerOptions: List<ResourceResponse> = emptyList(),
)
