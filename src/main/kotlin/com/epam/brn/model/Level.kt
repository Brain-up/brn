package com.epam.brn.model

import javax.persistence.*

@Entity
data class Level(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @Column(nullable = false)
    val level: String
) {
    @OneToMany(mappedBy = "level")
    val exerciseGroups: MutableSet<ExerciseGroup> = HashSet()
}