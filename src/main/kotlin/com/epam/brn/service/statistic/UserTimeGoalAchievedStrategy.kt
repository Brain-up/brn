package com.epam.brn.service.statistic

/**
 *@author Nikolai Lazarev
 */
interface UserTimeGoalAchievedStrategy<T> {

    /**
     * Should calculate how close the user to complete his training goal and return result in percent
     * @param time - time to calculate the result
     * @return goal achievement in percent
     */
    fun doStrategy(time: T): Int
}
