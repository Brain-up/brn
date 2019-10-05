package com.epam.brn.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

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