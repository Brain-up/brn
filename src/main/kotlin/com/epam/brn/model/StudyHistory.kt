package com.epam.brn.model

import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne

@Entity
data class StudyHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
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
)