package com.epam.brn.service.statistic.progress.status.impl

import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistic.progress.status.UserCoolDownRetriever
import org.springframework.stereotype.Component
import java.time.temporal.ChronoUnit
import java.util.TreeSet

/**
 *@author Nikolai Lazarev
 */

@Component
class UserCoolDownRetrieverImpl : UserCoolDownRetriever {
    override fun getMaximalUserCoolDown(period: Collection<StudyHistory>): Int {
        val periodSet = TreeSet(
            Comparator<StudyHistory> { current, next ->
                return@Comparator when {
                    current.startTime.isAfter(next.startTime) -> 1
                    current.startTime.isBefore(next.startTime) -> -1
                    else -> 0
                }
            }
        )
        periodSet.addAll(period)
        val durations: ArrayList<Int> = ArrayList()
        for (i in 1 until period.size) {
            durations.add(
                ChronoUnit.DAYS.between(periodSet.elementAt(i - 1).startTime, periodSet.elementAt(i).startTime).toInt()
            )
        }
        return durations.maxOrNull() ?: 0
    }
}
