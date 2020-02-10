package com.epam.brn.dto

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.constant.WordTypeEnum
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.LinkedList

data class TaskDtoForSentence(
    val id: Long? = null,
    val exerciseType: ExerciseTypeEnum = ExerciseTypeEnum.SENTENCE,
    @JsonIgnore
    val exerciseId: Long? = null,
    val name: String? = "",
    val serialNumber: Int? = 0,
    val template: String? = "",
    val correctAnswer: ResourceDto,
    val answerParts: List<ResourceDto> = LinkedList(),
    val answerOptions: Map<WordTypeEnum?, List<ResourceDto>> = HashMap()
)
