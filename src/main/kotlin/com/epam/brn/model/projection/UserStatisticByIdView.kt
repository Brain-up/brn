package com.epam.brn.model.projection

/**
 * All-time study statistics for a single user, carrying the user id so a batch
 * query can return the aggregate for many users at once (GROUP BY user).
 */
interface UserStatisticByIdView : UserStatisticView {
    val userId: Long
}
