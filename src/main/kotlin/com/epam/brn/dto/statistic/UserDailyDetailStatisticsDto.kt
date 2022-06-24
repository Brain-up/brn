package com.epam.brn.dto.statistic

data class UserDailyDetailStatisticsDto(

    val seriesName: String,

    val doneExercises: Int,

    val attempts: Int,

    val doneExercisesSuccessfullyFromFirstTime: Int,

    val listenWordsCount: Int
)
