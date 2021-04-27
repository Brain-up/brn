package com.epam.brn.service.statistic

import com.epam.brn.dto.statistic.Statistic
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

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
     * Should return statistic as Statistic implementation for period for from to to date
     * @param from - beginning date of the period
     * @param to - ending date of the period
     * @return list of implementations of Statistic
     */

    fun getUserStatisticForPeriod(
        @DateTimeFormat(pattern = "yyyy-MM-dd") from: LocalDate,
        @DateTimeFormat(pattern = "yyyy-MM-dd") to: LocalDate
    ): List<Statistic>
}
