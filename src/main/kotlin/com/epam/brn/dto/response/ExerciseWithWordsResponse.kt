package com.epam.brn.dto.response

import java.time.LocalDateTime
import java.time.ZoneOffset

data class ExerciseWithWordsResponse(
    val id: Long?,
    val name: String,
    val active: Boolean = true,
    val changedBy: String? = "InitialDataLoader",
    val changedWhen: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    val playWordsCount: Int? = 1,
    val wordsColumns: Int? = 3,
    val words: List<String>,
    val subGroupName: String?,
    val seriesName: String?
)
