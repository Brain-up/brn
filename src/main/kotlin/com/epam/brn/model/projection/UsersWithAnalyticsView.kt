package com.epam.brn.model.projection

import com.epam.brn.enums.BrnGender
import java.time.LocalDateTime

/**
 * StudyAnalyticsView.
 *
 * @author Andrey Samoylov
 */
interface UsersWithAnalyticsView {
    val id: Long
    val userId: String
    val fullName: String
    val email: String
    val bornYear: Int?
    val gender: BrnGender
    var active: Boolean
    val firstDone: LocalDateTime
    val lastDone: LocalDateTime
    var lastVisit: LocalDateTime
    val doneExercises: Int
    val spentTime: Long
    val studyDays: Int
}
