package com.epam.brn.dto.response

import com.epam.brn.dto.NoiseDto
import com.epam.brn.dto.SignalTaskDto
import java.time.LocalDateTime
import java.time.ZoneOffset

data class ExerciseWithTasksResponse(
    var seriesId: Long?,
    var id: Long?,
    var name: String,
    var level: Int? = 0,
    var noise: NoiseDto,
    var template: String? = "",
    var available: Boolean = true,
    var tasks: List<GeneralTaskResponse>,
    var signals: List<SignalTaskDto>,
    var active: Boolean = true,
    var changedBy: String? = "",
    var changedWhen: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    var isAudioFileUrlGenerated: Boolean = false,
    var playWordsCount: Int? = 1,
    var wordsColumns: Int? = 3,
)
