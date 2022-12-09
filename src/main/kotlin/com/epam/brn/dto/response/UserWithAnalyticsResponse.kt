package com.epam.brn.dto.response

import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.enums.AudiometryType
import com.epam.brn.enums.BrnGender
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import kotlin.time.Duration

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserWithAnalyticsResponse(
    val id: Long? = null,
    val userId: String? = null,
    val name: String?,
    val email: String?,
    val bornYear: Int?,
    val gender: BrnGender?,
    var active: Boolean = true,
    var firstDone: LocalDateTime? = null, // generally first done exercise
    var lastDone: LocalDateTime? = null, // generally last done exercise
    var lastWeek: List<DayStudyStatistic> = emptyList(),
    var studyDaysInCurrentMonth: Int = 0, // amount of days in current month when user made any exercises
    var diagnosticProgress: Map<AudiometryType, Boolean> = mapOf(AudiometryType.SIGNALS to true), // todo fill by user
    var doneExercises: Int = 0, // for all time
    var spentTime: Duration = Duration.ZERO, // spent time by doing exercises for all time
)
