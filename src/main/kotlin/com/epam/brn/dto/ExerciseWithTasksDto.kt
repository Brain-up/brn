package com.epam.brn.dto

import java.time.ZoneId
import java.time.ZonedDateTime

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
    var changedWhen: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
)
