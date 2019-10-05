package com.epam.brn.model

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne

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