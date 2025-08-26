package com.epam.brn.service.statistics

import com.epam.brn.dto.statistics.Statistics
import java.time.LocalDateTime

/**
 *@author Nikolai Lazarev
 */
interface UserPeriodStatisticsService<T : Statistics> {
    /**
     * Should return statistics as Statistics implementation for period for from to to date
     * @param from - beginning date of the period
     * @param to - ending date of the period
     * @param userId - id of the user for how get statistics
     * @return list of implementations of Statistics
     */
    fun getStatisticsForPeriod(
        from: LocalDateTime,
        to: LocalDateTime,
        userId: Long? = null,
    ): List<T>
}
