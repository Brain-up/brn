package com.epam.brn.dto

import com.epam.brn.model.ExerciseType

data class ExerciseDto(
    var seriesId: Long?,
    var id: Long?,
    var name: String?,
    var description: String?,
    var level: Int? = 0,
    var exerciseType: ExerciseType,
    var template: String? = "",
    var available: Boolean = true,
    var tasks: MutableSet<ShortTaskDto> = HashSet()
)
