package com.epam.brn.model

import java.time.LocalDate
import javax.persistence.*

@Entity
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val email: String,
    val birthDate: LocalDate? = null
) {
    @OneToMany(cascade = [(CascadeType.ALL)])
    val phoneNumbers: List<PhoneNumber>? = null
    @OneToOne(cascade = [(CascadeType.ALL)])
    @JoinColumn(name = "progress_id")
    val progress: Progress? = null
}