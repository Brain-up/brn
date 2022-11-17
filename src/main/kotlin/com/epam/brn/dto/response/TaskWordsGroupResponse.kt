package com.epam.brn.dto.response

import com.epam.brn.enums.ExerciseMechanism
import com.epam.brn.enums.ExerciseType
import com.epam.brn.enums.toMechanism
import com.epam.brn.enums.WordType

data class TaskWordsGroupResponse(
    val id: Long,
    val exerciseType: ExerciseType,
    val exerciseMechanism: ExerciseMechanism = exerciseType.toMechanism(),
    val name: String? = "",
    val serialNumber: Int? = 0,
    val template: String? = "",
    val answerOptions: Map<WordType?, List<ResourceResponse>> = HashMap()
)
