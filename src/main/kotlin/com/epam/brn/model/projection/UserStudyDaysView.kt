package com.epam.brn.model.projection

/**
 * Number of distinct calendar days a user exercised within a period.
 * Used to compute analytics for many users in a single aggregate query.
 */
interface UserStudyDaysView {
    val userId: Long
    val studyDays: Int
}
