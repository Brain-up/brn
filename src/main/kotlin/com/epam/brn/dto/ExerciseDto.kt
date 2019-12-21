package com.epam.brn.dto

import com.epam.brn.constant.ExerciseTypeEnum
import com.fasterxml.jackson.annotation.JsonIgnore

data class ExerciseDto(
    var seriesId: Long?,
    var id: Long?,
    var name: String?,
    var description: String?,
    var level: Short? = 0,
    var exerciseType: ExerciseTypeEnum,
    var template: String? = "",
    @JsonIgnore
    var available: Boolean? = null,
    var tasks: MutableSet<Long?> = HashSet()
)