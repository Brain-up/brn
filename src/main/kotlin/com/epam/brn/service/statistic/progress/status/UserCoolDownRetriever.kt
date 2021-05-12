package com.epam.brn.service.statistic.progress.status

import com.epam.brn.model.StudyHistory

/**
 *@author Nikolai Lazarev
 */
interface UserCoolDownRetriever {

    /**
     * Should return the biggest user break in training for the period in days count
     * @param period - The period for which looking for a break
     */
    fun getMaximalUserCoolDown(period: Collection<StudyHistory>): Int
}
