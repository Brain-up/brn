package com.epam.brn.dto

import java.time.LocalDateTime

data class ExerciseDto(
    var seriesId: Long?,
    var id: Long?,
    var name: String,
    var level: Int? = 0,
    var noise: NoiseDto,
    var template: String? = "",
    var available: Boolean = true,
    var tasks: MutableList<ShortTaskDto> = mutableListOf(),
    var signals: MutableList<SignalTaskDto> = mutableListOf(),
    var active: Boolean = true,
    var changedBy: String? = "",
    var changedWhen: LocalDateTime = LocalDateTime.now()
)
