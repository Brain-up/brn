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

@Entity
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
    val userAccount: UserAccount,
    @OneToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "exercise_id")
    val exercise: Exercise? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val doneTasksCount: Short,
    val successTasksCount: Short,
    val repetitionCount: Short
) {
    override fun toString(): String {
        return "StudyHistory(id=$id, userAccount=$userAccount, exercise=$exercise, startTime=$startTime, endTime=$endTime, doneTasksCount=$doneTasksCount, successTasksCount=$successTasksCount, repetitionCount=$repetitionCount)"
    }
}