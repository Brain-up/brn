package com.epam.brn.model

import com.epam.brn.dto.AudiometryLopotkoTaskDto
import com.epam.brn.dto.AudiometryMatrixTaskDto
import com.epam.brn.dto.AudiometrySignalsTaskDto
import com.epam.brn.enums.AudiometryType
import com.epam.brn.enums.EAR
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["audiometryGroup", "frequencyZone"])])
data class AudiometryTask(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // == for Lopotko diagnostic
    var level: Int? = 0,
    val audiometryGroup: String? = null, // А, Б, В, Г
    val frequencyZone: String? = null,
    val minFrequency: Int? = null,
    val maxFrequency: Int? = null,

    var count: Int? = 10,
    var showSize: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audiometry_id")
    var audiometry: Audiometry? = null,

    @ManyToMany(cascade = [CascadeType.MERGE, CascadeType.REFRESH])
    @JoinTable(
        name = "audiometry_task_resources",
        joinColumns = [JoinColumn(name = "audiometry_task_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "resource_id", referencedColumnName = "id")]
    )
    var answerOptions: MutableSet<Resource> = hashSetOf(),

    // == for frequency diagnostic
    val frequencies: String? = null,
    var ear: String = EAR.BOTH.name,
) {
    override fun toString() =
        "AudiometryTask(id=$id, order=$level, group=$audiometryGroup, frequencyZone=$frequencyZone, minFrequency=$minFrequency, maxFrequency=$maxFrequency, count=$count, ear =$ear, answerOptions=$answerOptions)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudiometryTask
        if (id != other.id) return false
        if (level != other.level) return false
        if (minFrequency != other.minFrequency) return false
        if (maxFrequency != other.maxFrequency) return false
        if (frequencyZone != other.frequencyZone) return false
        if (audiometryGroup != other.audiometryGroup) return false
        if (audiometry != other.audiometry) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + audiometryGroup.hashCode()
        result = 31 * result + frequencyZone.hashCode()
        return result
    }

    fun toDto(): Any {
        return when (audiometry!!.audiometryType) {
            AudiometryType.SIGNALS.name -> AudiometrySignalsTaskDto(
                id,
                EAR.valueOf(ear),
                frequencies!!.removeSurrounding("[", "]").split(", ").map { it.toInt() }
            )
            AudiometryType.SPEECH.name -> AudiometryLopotkoTaskDto(
                id,
                level!!,
                audiometryGroup!!,
                frequencyZone!!,
                minFrequency!!,
                maxFrequency!!,
                count!!,
                showSize!!,
                answerOptions
            )
            AudiometryType.SPEECH.name -> AudiometryMatrixTaskDto(
                id,
                count!!,
                answerOptions
            )
            else -> throw IllegalArgumentException("${audiometry!!.audiometryType} does not supported!")
        }
    }
}
