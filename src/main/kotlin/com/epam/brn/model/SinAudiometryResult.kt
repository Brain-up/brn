package com.epam.brn.model

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class SinAudiometryResult(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val frequency: Int,
    val soundLevel: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audiometry_history_id")
    var audiometryHistory: AudiometryHistory? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SinAudiometryResult
        if (id != other.id) return false
        if (frequency != other.frequency) return false
        if (soundLevel != other.soundLevel) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + frequency.hashCode()
        result = 31 * result + soundLevel.hashCode()
        return result
    }

    override fun toString() = "SinAudiometryResult(id=$id, frequency=$frequency, soundLevel=$soundLevel, audiometryHistoryId=${audiometryHistory?.id})"
}
