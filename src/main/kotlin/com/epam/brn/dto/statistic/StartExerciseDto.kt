package com.epam.brn.dto.statistic

/**
 *@author Nikolai Lazarev
 */
class StartExerciseDto(
    val seriesName: String?,
    val subSeriesName: String?,
    val id: Long,
    val level: Int,
    val spentTime: Int,
    val tasksCount: Int,
    val wrongAnswers: Int,
    val repetition: Int
)
