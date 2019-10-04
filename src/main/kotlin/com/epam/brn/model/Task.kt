package com.epam.brn.model

import javax.persistence.*

@Entity
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @ManyToOne
    @JoinColumn(name = "exercise_id")
    val exercise: Exercise,
    @OneToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "resource_id")
    val resourceId: Resource,
    @OneToMany(cascade = [(CascadeType.ALL)])
    val arrayAnswers: MutableSet<Answer> = HashSet()
)