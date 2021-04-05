package com.epam.brn.service.statistic

/**
 *@author Nikolai Lazarev
 */
interface UserTimeGoalAchievedStrategy<T> {

    /**
     * Check if user achieved time goal or not
     * @param time - time to calculate the result
     * @return - false if user didn't achieve the goal for the month
     * @return - true if user achieved the goal for the month
     */
    fun isGoalAchieved(time: T): Boolean
}
