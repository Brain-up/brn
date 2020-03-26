package com.epam.brn.dto

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.constant.WordType
import com.fasterxml.jackson.annotation.JsonIgnore

data class TaskDtoForWordsSequences(
    val id: Long? = null,
    val exerciseType: ExerciseTypeEnum = ExerciseTypeEnum.WORDS_SEQUENCES,
    @JsonIgnore
    val exerciseId: Long? = null,
    val name: String? = "",
    val serialNumber: Int? = 0,
    val template: String? = "",
    val answerOptions: Map<WordType?, List<ResourceDto>> = HashMap()
)
