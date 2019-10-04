package com.epam.brn.model

import javax.persistence.*

@Entity
data class Exercise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    val description: String? = "",
    val level: Short? = 0,
    @ManyToOne
    @JoinColumn(name = "exercise_series_id")
    var exerciseSeries: ExerciseSeries
)