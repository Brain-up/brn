package com.epam.brn.dto

import com.epam.brn.model.ExerciseType

data class ExerciseDto(
    var seriesId: Long?,
    var id: Long?,
    var name: String?,
    var pictureUrl: String?,
    var description: String?,
    var level: Int? = 0,
    var noise: NoiseDto,
    var exerciseType: ExerciseType,
    var template: String? = "",
    var available: Boolean = true,
    var tasks: MutableList<ShortTaskDto> = mutableListOf(),
    var signals: MutableList<SignalDto> = mutableListOf()
)
