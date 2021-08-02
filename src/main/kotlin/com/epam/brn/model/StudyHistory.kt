package com.epam.brn.model

import com.epam.brn.dto.StudyHistoryDto
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "exercise_id", "startTime"])],
    indexes = [Index(name = "study_history_ix_user_exercise", columnList = "user_id,exercise_id")]
)
data class StudyHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var userAccount: UserAccount,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    var exercise: Exercise,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime? = null,
    var executionSeconds: Int,
    var tasksCount: Short,
    var wrongAnswers: Int,
    var replaysCount: Int,
    var repetitionIndex: Float = 0.0f,
    var rightAnswersIndex: Float = 1.0f,

) {
    override fun toString() =
        "StudyHistory(id=$id, userAccount=$userAccount, exercise=$exercise, startTime=$startTime, endTime=$endTime, tasksCount=$tasksCount, wrongAnswers=$wrongAnswers)"

    fun toDto() = StudyHistoryDto(
        id = this.id,
        exerciseId = this.exercise.id!!,
        startTime = this.startTime,
        endTime = this.endTime,
        executionSeconds = this.executionSeconds,
        tasksCount = this.tasksCount,
        wrongAnswers = this.wrongAnswers,
        replaysCount = this.replaysCount
    )
}
