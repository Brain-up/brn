package com.epam.brn.model

import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "audiometry_task_id", "startTime"])],
)
class AudiometryHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var userAccount: UserAccount,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audiometry_task_id")
    var audiometryTask: AudiometryTask,

    var startTime: LocalDateTime,
    var endTime: LocalDateTime? = null,
    var executionSeconds: Int?,
    var tasksCount: Short,
    var rightAnswers: Int,
    var replaysCount: Int? = null,
    var repetitionIndex: Float? = null,
    var rightAnswersIndex: Float? = null

) {
    override fun toString() =
        "AudiometryHistory(id=$id, userAccount=$userAccount, audiometryTask=audiometryTask, startTime=$startTime, endTime=$endTime, tasksCount=$tasksCount, rightAnswers=$rightAnswers)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudiometryHistory
        if (id != other.id) return false
        if (startTime != other.startTime) return false
        return true
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
