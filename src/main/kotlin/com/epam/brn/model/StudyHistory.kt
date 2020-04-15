package com.epam.brn.model

import com.epam.brn.dto.StudyHistoryDto
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "exercise_id"])],
    indexes = [Index(name = "study_history_ix_user_exercise", columnList = "user_id,exercise_id")]
)
data class StudyHistory(
    @Id
    @GeneratedValue(generator = "study_history_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
        name = "study_history_id_seq",
        sequenceName = "study_history_id_seq",
        allocationSize = 50
    )
    val id: Long? = null,

    @Column(name = "user_id", unique = true)
    var userId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    var exercise: Exercise,

    var startTime: LocalDateTime? = null,
    var endTime: LocalDateTime? = null,
    var tasksCount: Short? = null,
    var repetitionIndex: Float? = null
) {
    override fun toString() =
        "StudyHistory(id=$id, userAccount=$userId, exercise=$exercise, startTime=$startTime, endTime=$endTime, tasksCount=$tasksCount, repetitionIndex=$repetitionIndex)"

    fun toDto() = StudyHistoryDto(
        id = this.id,
        userId = this.userId,
        exerciseId = this.exercise.id,
        startTime = this.startTime,
        endTime = this.endTime,
        tasksCount = this.tasksCount,
        repetitionIndex = this.repetitionIndex
    )
}
