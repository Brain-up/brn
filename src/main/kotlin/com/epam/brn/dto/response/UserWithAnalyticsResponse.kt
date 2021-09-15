package com.epam.brn.dto.response

import com.epam.brn.enums.AudiometryType
import com.epam.brn.model.Gender
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import java.time.ZoneOffset

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserWithAnalyticsResponse(
    val id: Long? = null,
    val userId: String? = null,
    val name: String?,
    val email: String?,
    val bornYear: Int?,
    val gender: Gender?,
    var active: Boolean = true,
    var firstDone: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    var lastDone: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    var lastWeek: List<Int> = listOf(30, 0, 32, 33, 45, 21, 40), // todo fill by user
    var workDayByLastMonth: Int = 2, // todo fill by user
    var diagnosticProgress: Map<AudiometryType, Boolean> = mapOf(AudiometryType.SIGNALS to true), // todo fill by user
)
