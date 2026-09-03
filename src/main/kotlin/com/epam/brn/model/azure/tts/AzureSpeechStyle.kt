package com.epam.brn.model.azure.tts

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany

@Entity
class AzureSpeechStyle(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "name", nullable = false, unique = true)
    val name: String,
    @ManyToMany(mappedBy = "styleList")
    val voices: MutableList<AzureVoiceInfo>? = null,
)
