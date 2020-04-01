package com.epam.brn.dto

import com.epam.brn.model.ExerciseType
import com.epam.brn.model.WordType
import com.fasterxml.jackson.annotation.JsonIgnore

data class TaskDtoForWordsSequences(
    val id: Long? = null,
    val exerciseType: ExerciseType = ExerciseType.WORDS_SEQUENCES,
    @JsonIgnore
    val exerciseId: Long? = null,
    val name: String? = "",
    val serialNumber: Int? = 0,
    val template: String? = "",
    val answerOptions: Map<WordType?, List<ResourceDto>> = HashMap()
)
