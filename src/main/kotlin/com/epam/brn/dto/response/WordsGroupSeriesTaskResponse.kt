package com.epam.brn.dto.response

import com.epam.brn.dto.ResourceDto
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.WordType
import com.fasterxml.jackson.annotation.JsonIgnore

data class WordsGroupSeriesTaskResponse(
    val id: Long,
    val exerciseType: ExerciseType,
    @JsonIgnore
    val exerciseId: Long? = null,
    val name: String? = "",
    val serialNumber: Int? = 0,
    val template: String? = "",
    val answerOptions: Map<WordType?, List<ResourceDto>> = HashMap()
)
