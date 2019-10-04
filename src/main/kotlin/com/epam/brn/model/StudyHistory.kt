package com.epam.brn.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class StudyHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @OneToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "user_id")
    val user: User,
    @OneToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "exercise_id")
    val exercise: Exercise? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val doneTasksCount: Short,
    val successTasksCount: Short,
    val repetitionCount: Short
)