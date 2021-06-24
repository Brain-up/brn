package com.epam.brn.service.statistic.progress.status

import java.time.LocalDate

/**
 * This interface provides functionality to get user's cool downs in period of time
 * Cool down - is a period when user didn't exercise at all
 */
interface UserRestTimeRetriever {

    /**
     * Should return the biggest user break in training for the period in days count
     * @param from - start date of the period for which to look break for
     * @param to - end date of the period for which to look break for
     * @param userId - id of the user for which to look break for
     * @return the biggest break in days
     */
    fun getMaximalUserRestTime(userId: Long? = null, from: LocalDate, to: LocalDate): Int
}
