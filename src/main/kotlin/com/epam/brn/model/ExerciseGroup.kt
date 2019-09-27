package com.epam.brn.model

import javax.persistence.*

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
    @OneToMany(mappedBy = "exerciseGroup")
    val exercises: MutableSet<Exercise> = HashSet()
    @ManyToOne
    val level: Level? = null
}
