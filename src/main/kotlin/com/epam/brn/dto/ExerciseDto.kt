package com.epam.brn.dto

data class ExerciseDto(
    var seriesId: Long?,
    var id: Long?,
    var name: String,
    var level: Int? = 0,
    var noise: NoiseDto,
    var template: String? = "",
    var available: Boolean = true,
    var tasks: MutableList<ShortTaskDto> = mutableListOf(),
    var signals: MutableList<SignalDto> = mutableListOf()
)
