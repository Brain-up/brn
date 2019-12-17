package com.epam.brn.dto

import com.epam.brn.constant.SeriesTypeEnum
import com.fasterxml.jackson.annotation.JsonIgnore

data class TaskDtoForWordsSequences(
    val id: Long? = null,
    val seriesType: SeriesTypeEnum = SeriesTypeEnum.WORDS_SEQUENCES,
    @JsonIgnore
    val exerciseId: Long? = null,
    val name: String? = "",
    val serialNumber: Int? = 0,
    val answerOptions: MutableSet<MutableSet<ResourceDto>> = HashSet()
)