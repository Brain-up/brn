package com.epam.brn.dto

import com.epam.brn.model.ExerciseType
import com.epam.brn.model.WordType
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.LinkedList

data class TaskDtoForSentence(
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
