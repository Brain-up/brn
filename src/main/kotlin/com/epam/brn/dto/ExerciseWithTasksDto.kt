package com.epam.brn.dto

data class ExerciseWithTasksDto(
    var seriesId: Long?,
    var id: Long?,
    var name: String,
    var level: Int? = 0,
    var noise: NoiseDto,
    var template: String? = "",
    var available: Boolean = true,
    var tasks: List<GeneralTaskDto>,
    var signals: List<SignalTaskDto>,
)
