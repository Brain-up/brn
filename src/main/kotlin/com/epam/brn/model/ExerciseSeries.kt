package com.epam.brn.model

import javax.persistence.*

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
)