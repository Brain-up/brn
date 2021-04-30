package com.epam.brn.service.statistic

import com.epam.brn.dto.statistic.Statistic
import java.time.LocalDate

/**
 *@author Nikolai Lazarev
 */
interface UserPeriodStatisticService<T : Statistic> {

    /**
     * Should return statistic as Statistic implementation for period for from to to date
     * @param from - beginning date of the period
     * @param to - ending date of the period
     * @return list of implementations of Statistic
     */
    fun getStatisticForPeriod(from: LocalDate, to: LocalDate): List<T>
}
