package com.epam.brn.dto

import com.epam.brn.enums.ExerciseType
import com.epam.brn.enums.WordType
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.LinkedList

data class TaskDtoFor3Series(
    val id: Long? = null,
    val exerciseType: ExerciseType = ExerciseType.SENTENCE,
    @JsonIgnore
    val exerciseId: Long? = null,
    val name: String? = "",
    val serialNumber: Int? = 0,
    val template: String? = "",
    val correctAnswer: ResourceDto,
    val answerParts: List<ResourceDto> = LinkedList(),
    val answerOptions: Map<WordType?, List<ResourceDto>> = HashMap()
)
