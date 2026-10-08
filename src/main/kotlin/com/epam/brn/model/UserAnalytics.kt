package com.epam.brn.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * A precomputed daily snapshot of a user's study analytics (filled by [com.epam.brn.job.UserAnalyticsJob]).
 * One row per user/role/[snapshotDate]; history accumulates day by day for trend reporting.
 */
@Entity
@Table(
    indexes = [
        Index(name = "user_analytics_ix_snapshot_date", columnList = "snapshot_date"),
        Index(name = "user_analytics_ix_role_name", columnList = "role_name"),
    ],
)
class UserAnalytics(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "snapshot_date")
    val snapshotDate: LocalDate,
    val userId: Long,
    @Column(name = "role_name")
    val roleName: String,
    val firstDone: LocalDateTime?,
    val lastDone: LocalDateTime?,
    val spentTime: Long?,
    val doneExercises: Int?,
    val studyDays: Int?,
) {
    override fun toString(): String = "UserAnalytics(id=$id, snapshotDate=$snapshotDate, userId=$userId, roleName='$roleName', " +
        "firstDone=$firstDone, lastDone=$lastDone, spentTime=$spentTime, doneExercises=$doneExercises, studyDays=$studyDays)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserAnalytics

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (snapshotDate != other.snapshotDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + userId.hashCode()
        result = 31 * result + snapshotDate.hashCode()
        return result
    }
}
