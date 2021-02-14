package com.epam.brn.service

import com.epam.brn.dto.statistic.StartExerciseDto
import java.time.LocalDateTime
import java.time.Year

/**
 *@author Nikolai Lazarev
 */
interface UserStatisticService {

    /**
     *Should return statistic for month
     * @param month - month to calculate statistic for
     * @return map of day to time (how much user spent in this day for exercises) for in minutes
     */
    fun getUserMonthStatistic(month: Int, year: Int?): Map<Int, Int>

    /**
     * Should return a year statistic
     * @param year - year to calculate statistic for
     * @return map of Month to Time in minutes
     */
    fun getUserYearStatistic(year: Year): Map<Int, Int>

    /**
     * Should return statistic for specific day
     * @param year - which year use to calculate statistic
     * @param month - which month of year parameter use to calculate statistic
     * @param day - which day of month parameter use to calculate statistic
     * @return map of an exercise start time to information about the exercise
     */
    fun getUserDayStatistic(month: Int, day: Int, year: Year): Map<LocalDateTime, StartExerciseDto>
}
