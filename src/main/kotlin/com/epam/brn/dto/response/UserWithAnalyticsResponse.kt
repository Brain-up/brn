package com.epam.brn.dto.response

import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.enums.AudiometryType
import com.epam.brn.model.Gender
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserWithAnalyticsResponse(
    val id: Long? = null,
    val userId: String? = null,
    val name: String?,
    val email: String?,
    val bornYear: Int?,
    val gender: Gender?,
    var active: Boolean = true,
    var firstDone: LocalDateTime? = null, // generally first done exercise
    var lastDone: LocalDateTime? = null, // generally last done exercise
    var lastWeek: List<DayStudyStatistic> = emptyList(),
    var studyDaysInLastMonth: Int = 0, // amount of days in last month when user made any exercises
    var diagnosticProgress: Map<AudiometryType, Boolean> = mapOf(AudiometryType.SIGNALS to true), // todo fill by user
)
