package com.epam.brn.model

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class ExerciseSeries(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    val name: String,
    @Column
    val description: String,
    @ManyToOne
    @JoinColumn(name = "exercise_group_id")
    val exerciseGroup: ExerciseGroup,
    @OneToMany(mappedBy = "exerciseSeries", cascade = [(CascadeType.ALL)])
    val exercises: MutableSet<Exercise> = HashSet()
) {
    override fun toString(): String {
        return "ExerciseSeries(id=$id, name='$name', description='$description', exerciseGroup=$exerciseGroup, exercises=$exercises)"
    }
}