package com.epam.brn.model

import org.springframework.beans.factory.annotation.Value

interface UserDailyDetailStatisticsProjection {

    @get:Value("#{target.series_name}")
    val seriesName: String

    @get:Value("#{target.done_exercises}")
    val doneExercises: Int?

    val attempts: Int?

    @get:Value("#{target.done_exercises_successfully_from_first_time}")
    val doneExercisesSuccessfullyFromFirstTime: Int?

    @get:Value("#{target.listen_words_count}")
    val listenWordsCount: Int?
}
