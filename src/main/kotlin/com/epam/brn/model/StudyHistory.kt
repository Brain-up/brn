package com.epam.brn.model

import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "exercise_id"])
    ]
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
    @OneToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "user_id")
    var userAccount: UserAccount?,
    @OneToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "exercise_id")
    var exercise: Exercise? = null,
    var startTime: LocalDateTime?,
    var endTime: LocalDateTime?,
    var doneTasksCount: Short?,
    var successTasksCount: Short?,
    var repetitionCount: Short?
) {
    constructor() : this(null, null, null, null, null, null, null, null)

    override fun toString(): String {
        return "StudyHistory(id=$id, userAccount=$userAccount, exercise=$exercise, startTime=$startTime, endTime=$endTime, doneTasksCount=$doneTasksCount, successTasksCount=$successTasksCount, repetitionCount=$repetitionCount)"
    }
}