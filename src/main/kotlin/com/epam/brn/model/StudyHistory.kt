package com.epam.brn.model

import com.epam.brn.dto.StudyHistoryDto
import java.time.LocalDateTime
import java.time.ZoneOffset
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "exercise_id", "startTime"])],
    indexes = [Index(name = "study_history_ix_user_exercise", columnList = "user_id,exercise_id")],
)
class StudyHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = true, updatable = false)
    var userAccount: UserAccount,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    var exercise: Exercise,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime? = null,
    var spentTimeInSeconds: Long? =
        endTime
            ?.toInstant(ZoneOffset.UTC)
            ?.epochSecond
            ?.minus(startTime.toInstant(ZoneOffset.UTC).epochSecond),
    var executionSeconds: Int,
    var tasksCount: Short,
    var wrongAnswers: Int,
    var replaysCount: Int,
    var repetitionIndex: Float? = 0.0f,
    var rightAnswersIndex: Float? = 1.0f,
) {
    fun toDto() =
        StudyHistoryDto(
            id = this.id,
            exerciseId = this.exercise.id!!,
            startTime = this.startTime,
            endTime = this.endTime,
            executionSeconds = this.executionSeconds,
            tasksCount = this.tasksCount,
            wrongAnswers = this.wrongAnswers,
            replaysCount = this.replaysCount,
        )
}
