package com.epam.brn.model.projection

import java.time.LocalDateTime

interface UserStatisticsWithIdView {
    val userId: Long
    val firstStudy: LocalDateTime?
    val lastStudy: LocalDateTime?
    val spentTime: Long
    val doneExercises: Int
}
