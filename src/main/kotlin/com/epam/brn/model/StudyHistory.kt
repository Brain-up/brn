package com.epam.brn.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class StudyHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @OneToOne
    @JoinColumn(name = "user_id")
    val user: User,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime

) {
    @OneToOne
    @JoinColumn(name = "exercise_id")
    val exercise: Exercise? = null
}