package com.epam.brn.service.statistic.impl.strategy

import com.epam.brn.service.statistic.UserTimeGoalAchievedStrategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 *@author Nikolai Lazarev
 */

@Component
class DayUserGoalAchievedStrategy : UserTimeGoalAchievedStrategy<Int> {

    @Value("\${brn.statistic.goal.day}")
    var defaultMinutesPerDaySuccess: Int = 0

    override fun doStrategy(time: Int): Int {
        return time / ((defaultMinutesPerDaySuccess * 60) / 100)
    }
}
