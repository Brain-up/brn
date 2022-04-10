package com.epam.brn.model.azure.tts

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany

@Entity
class AzureSpeechStyle(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "name", nullable = false, unique = true)
    val name: String,
    @ManyToMany(mappedBy = "styleList")
    val voices: MutableList<AzureVoiceInfo>? = null
)
