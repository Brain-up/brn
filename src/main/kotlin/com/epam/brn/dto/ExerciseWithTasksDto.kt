package com.epam.brn.dto

import java.time.LocalDateTime
import java.time.ZoneOffset

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
    var active: Boolean = true,
    var changedBy: String? = "",
    var changedWhen: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
)
