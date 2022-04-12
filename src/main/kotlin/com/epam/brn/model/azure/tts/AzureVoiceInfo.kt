package com.epam.brn.model.azure.tts

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany

@Entity
class AzureVoiceInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "name", nullable = false)
    val name: String? = null,
    @Column(name = "display_name", nullable = false)
    val displayName: String? = null,
    @Column(name = "local_name")
    val localName: String? = null,
    @Column(name = "short_name", nullable = false, unique = true)
    val shortName: String,
    @Column(name = "gender", nullable = false)
    val gender: String,
    @Column(name = "locale", nullable = false)
    val locale: String,
    @Column(name = "locale_name")
    val localeName: String? = null,
    @Column(name = "sample_rate_hertz")
    val sampleRateHertz: String? = null,
    @Column(name = "voice_type")
    val voiceType: String? = null,
    @Column(name = "status")
    val status: String? = null,
    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "azure_voice_style",
        joinColumns = [JoinColumn(name = "style_id")],
        inverseJoinColumns = [JoinColumn(name = "voice_id")]
    )
    val styleList: MutableSet<AzureSpeechStyle> = mutableSetOf()
)
