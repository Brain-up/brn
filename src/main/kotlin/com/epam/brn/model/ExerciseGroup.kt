package com.epam.brn.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class ExerciseGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    val name: String,
    @Column
    val description: String
) {
    @OneToMany(mappedBy = "exerciseSeries")
    val exercises: MutableSet<Exercise> = HashSet()

    override fun toString(): String {
        return "ExerciseGroup(id=$id, name='$name', description='$description', exercises=$exercises)"
    }
}
