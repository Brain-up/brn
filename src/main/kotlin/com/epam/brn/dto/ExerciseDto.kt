package com.epam.brn.dto

import java.time.LocalDateTime
import java.time.ZoneOffset

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
    var changedWhen: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    var exerciseIndex: Int = 1,
    var isAudioFileUrlGenerateDynamically: Boolean = false
)
