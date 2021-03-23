package com.epam.brn.dto

import com.epam.brn.model.ExerciseType

data class ExerciseSignalDto(
    var seriesId: Long?,
    var id: Long?,
    var name: String?,
    var description: String?,
    var level: Int? = 0,
    var exerciseType: ExerciseType,
    var available: Boolean = true,
    var signals: MutableSet<SignalTaskDto> = HashSet()
)
