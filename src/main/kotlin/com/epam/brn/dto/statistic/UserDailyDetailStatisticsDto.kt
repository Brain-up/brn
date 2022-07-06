package com.epam.brn.dto.statistic

data class UserDailyDetailStatisticsDto(

    /** Name of series */
    val seriesName: String,

    /** Count of all done exercises */
    val allDoneExercises: Int,

    /** Count of unique done exercises */
    val uniqueDoneExercises: Int,

    /** Repeated exercises count */
    val repeatedExercises: Int,

    /** Count of success exercise from first time */
    val doneExercisesSuccessfullyFromFirstTime: Int,

    /** Count listened words in a day */
    val listenWordsCount: Int
)
