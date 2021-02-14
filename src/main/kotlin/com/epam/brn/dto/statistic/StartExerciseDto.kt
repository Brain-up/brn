package com.epam.brn.dto.statistic

/**
 *@author Nikolai Lazarev
 */
class StartExerciseDto (
    val exerciseSeriesName: String,
    val exerciseSubSeriesName: String,
    val exerciseId: Long,
    val exerciseLevel: Int,
    val exerciseSpentTime: Int,
    val exerciseTasksCount: Int,
    val exerciseWrongAnswers: Int,
    val exerciseRepetition: Int
)