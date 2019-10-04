package com.epam.brn.model

import javax.persistence.*

@Entity
data class Progress(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @Column(nullable = false)
    val progress: String
) {
    @OneToOne(cascade = [(CascadeType.ALL)])
    val exerciseGroup: ExerciseGroup? = null
}