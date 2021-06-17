package com.epam.brn.dto

import java.time.ZoneId
import java.time.ZonedDateTime

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
    var changedBy: String? = "InitialDataLoader",
    var changedWhen: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
)
