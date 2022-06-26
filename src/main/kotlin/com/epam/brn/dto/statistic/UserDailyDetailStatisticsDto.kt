package com.epam.brn.dto.statistic

data class UserDailyDetailStatisticsDto(

    /** Name of series */
    var seriesName: String,

    /** Count of done exercise. */
    var doneExercises: Int,

    /** Count attempts to done exercise */
    var attempts: Int,

    /** Count of success exercise from first time */
    var doneExercisesSuccessfullyFromFirstTime: Int,

    /** Count listened words */
    var listenWordsCount: Int
)
