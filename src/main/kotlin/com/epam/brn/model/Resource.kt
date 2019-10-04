package com.epam.brn.model

import javax.persistence.*

@Entity
data class Resource(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @Column(nullable = false)
    val audioFileId: String,
    @Column(nullable = false)
    val word: String,
    @Column(nullable = false)
    val pictureId: String,
    @Column(nullable = false)
    val soundsCount: Int

)