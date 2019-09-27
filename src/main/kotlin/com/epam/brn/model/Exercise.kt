package com.epam.brn.model

import javax.persistence.*

@Entity
data class Exercise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    val tasksCount: Int? = null
) {
    @ManyToOne
    lateinit var exerciseGroup: ExerciseGroup

    constructor(name: String, exerciseGroup: ExerciseGroup) : this(name = name) {
        this.exerciseGroup = exerciseGroup
    }

}