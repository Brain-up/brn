package com.epam.brn.model

import javax.persistence.*

@Entity
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val email: String
) {
    @OneToMany(cascade = [(CascadeType.ALL)])
    val phoneNumbers: List<PhoneNumber>? = null
    @ManyToOne
    @JoinColumn(name = "level_id")
    val level: Level? = null}