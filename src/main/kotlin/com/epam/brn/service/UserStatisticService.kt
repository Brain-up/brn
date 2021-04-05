package com.epam.brn.service

import com.epam.brn.dto.statistic.Statistic

/**
 *@author Nikolai Lazarev
 */
interface UserStatisticService {

    /**
     * Should return subGroups progress for user
     * @param subGroupsIds - list of sub groups ids which statistic should be returned
     * @return list of information how many exercises subGroup has and how many exercises user has completed in the
     * subGroup
     */
    fun getSubGroupStatistic(subGroupsIds: List<Long>): List<Statistic>

    /**
     *Should return statistic for month
     * @param month - month to calculate statistic for
     * @return map of day to specific for month statistic
     */
    fun getUserMonthStatistic(month: Int? = null, year: Int? = null): Map<Int, Statistic>

    /**
     * Should return a year statistic
     * @param year - year to calculate statistic for
     * @return map of year to specific for year statistic
     */
    fun getUserYearStatistic(year: Int? = null): Map<Int, Statistic>

    /**
     * Should return statistic for specific day
     * @param year - which year use to calculate statistic
     * @param month - which month of year parameter use to calculate statistic
     * @param day - which day of month parameter use to calculate statistic
     * @return map of an exercise start time to information about the exercise
     */
    fun getUserDayStatistic(month: Int? = null, day: Int? = null, year: Int? = null): Map<String, Statistic>
}
