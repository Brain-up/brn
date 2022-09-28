package com.epam.brn.dto

import com.epam.brn.dto.response.GeneralTaskResponse
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
    var tasks: List<GeneralTaskResponse> = mutableListOf(),
    var signals: List<SignalTaskDto> = mutableListOf(),
    var active: Boolean = true,
    var changedBy: String? = "InitialDataLoader",
    var changedWhen: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    var isAudioFileUrlGenerated: Boolean = false,
    var playWordsCount: Int? = 1,
    var wordsColumns: Int? = 3,
)
