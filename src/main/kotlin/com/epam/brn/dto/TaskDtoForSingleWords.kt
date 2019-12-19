package com.epam.brn.dto

import com.epam.brn.constant.SeriesTypeEnum
import com.fasterxml.jackson.annotation.JsonIgnore

data class TaskDtoForSingleWords(
    val id: Long? = null,
    val seriesType: SeriesTypeEnum = SeriesTypeEnum.SINGLE_WORDS,
    @JsonIgnore
    val exerciseId: Long? = null,
    val name: String? = "",
    val correctAnswer: ResourceDto? = null,
    val serialNumber: Int? = 0,
    val answerOptions: MutableSet<ResourceDto> = HashSet()
)