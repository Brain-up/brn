package com.epam.brn.model

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Index
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Id

@Entity
@Table(indexes = [Index(name = "user_analytics_ix_role_name", columnList = "role_name")])
class UserAnalytics(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val userId: Long,

    val firstDone: LocalDateTime?,

    val lastDone: LocalDateTime?,

    val spentTime: Long?,

    val doneExercises: Int?,

    val studyDays: Int?,

    @Column(name = "role_name")
    val roleName: String
) {
    override fun toString(): String {
        return "UserAnalytics(id=$id, userId=$userId, firstDone=$firstDone, lastDone=$lastDone, spentTime=$spentTime, doneExercises=$doneExercises, studyDays=$studyDays, roleName='$roleName')"
    }
}
